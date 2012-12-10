/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * The "dexcheck" tool is intended to check the method numbers of a DEX file, 
 * when the method count over the threshold, it will show a warning message.
 */

#include "libdex/DexFile.h"

#include "libdex/CmdUtils.h"
#include "libdex/DexCatch.h"
#include "libdex/DexClass.h"
#include "libdex/DexDebugInfo.h"
#include "libdex/DexOpcodes.h"
#include "libdex/DexProto.h"
#include "libdex/InstrUtils.h"
#include "libdex/SysUtil.h"

#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <errno.h>
#include <assert.h>

#define DEFAULT_THRESHOLD (62260)

static const char *gProgName = "dexcheck";

/* command-line options */
struct Options {
    bool force_show;
    char *out_file;
    unsigned threshold;
};

struct Options gOptions;

/*
 * Dump the file header.
 */
void dumpFileHeader(const char *fileName, const DexFile *pDexFile)
{
    const DexHeader *pHeader = pDexFile->pHeader;

    if ((pHeader->methodIdsSize > gOptions.threshold) || (gOptions.force_show == true)) {
        if (gOptions.out_file) {
            FILE *fp = fopen(gOptions.out_file, "a+");
            if (fp) {
                fprintf(fp, "%s(%d) reaches method threshold(%d)\n", fileName, pHeader->methodIdsSize, gOptions.threshold);
                fclose(fp);
            } else {
                fprintf(stderr, "File open failed: %s\n", gOptions.out_file);
            }
        } else {
            fprintf(stderr, "DEX method count over threshold warning!!!\n");
            fprintf(stderr, "%s(%d) reaches method threshold(%d)\n", fileName, pHeader->methodIdsSize, gOptions.threshold);
        }
    }
}

/*
 * Process one file.
 */
int process(const char *fileName)
{
    DexFile *pDexFile = NULL;
    MemMapping map;
    bool mapped = false;
    int result = -1;
    int flags = kDexParseVerifyChecksum;

    if (dexOpenAndMap(fileName, NULL, &map, false) != 0) {
        return result;
    }
    mapped = true;


    pDexFile = dexFileParse((u1*)map.addr, map.length, flags);
    if (pDexFile == NULL) {
        fprintf(stderr, "ERROR: DEX parse failed\n");
        goto bail;
    }

    dumpFileHeader(fileName, pDexFile);

    result = 0;

bail:
    if (mapped) {
        sysReleaseShmem(&map);
    }
    if (pDexFile != NULL) {
        dexFileFree(pDexFile);
    }
    return result;
}


/*
 * Show usage.
 */
void usage(void)
{
    fprintf(stderr, "Copyright (C) 2012 Mediatek Inc.\n\n");
    fprintf(stderr, "%s: [-s] [-o output_file] [-t threshold] dexfile...\n", gProgName);
    fprintf(stderr, "\n");
    fprintf(stderr, " -o : output the warning message to the file\n");
    fprintf(stderr, " -s : bypass threshold and force to show warning message\n");
    fprintf(stderr, " -t : change threshold to warning, default:%d\n", DEFAULT_THRESHOLD);
}

/*
 * Parse args.
 *
 * I'm not using getopt_long() because we may not have it in libc.
 */
int main(int argc, char *const argv[])
{
    bool wantUsage = false;
    int ic;
    int result = 0;

    gOptions.threshold = DEFAULT_THRESHOLD;
    gOptions.out_file = NULL;
    gOptions.force_show = false;

    while (1) {
        ic = getopt(argc, argv, "st:o:");
        if (ic < 0)
            break;

        switch (ic) {
            case 't':       // threshold
                gOptions.threshold = atoi(optarg);
                if (gOptions.threshold > 65535) {
                    fprintf(stderr, "Invalid threshold value %s\n", optarg);
                    wantUsage = true;
                }
                break;
            case 'o':       // output file name
                gOptions.out_file = optarg;
                break;
            case 's':
                gOptions.force_show = true;
                break;
            default:
                wantUsage = true;
                break;
        }
    }

    if (optind == argc) {
        fprintf(stderr, "%s: no file specified\n", gProgName);
        wantUsage = true;
    }

    if (wantUsage) {
        usage();
        return 2;
    }

    while (optind < argc) {
        result |= process(argv[optind++]);
    }

    return (result != 0);
}

