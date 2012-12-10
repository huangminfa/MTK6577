// Ini_Unite.cpp: implementation of the Ini_Unite class.
//
//////////////////////////////////////////////////////////////////////

#include "CheckSum_Generate.h"
#include "Ini_Unit.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

using std::string;

#ifdef _DEBUG
#undef THIS_FILE
static char THIS_FILE[]=__FILE__;
#define new DEBUG_NEW
#endif

#ifdef WIN32
#else
#define MAX_PATH 256
#endif

#define LINE_LEN 256


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

Ini_Unit::Ini_Unit()
{
}

Ini_Unit::Ini_Unit(string path)
{
	ini_path = path;
}

Ini_Unit::~Ini_Unit()
{
}

int Ini_Unit::Write (const char* section,const char* lpKeyName, const char* lpString)
{
	//return ::WriteProfileString ((const char*)ini_path, lpKeyName, lpString);
	//return WritePrivateProfileString (section, lpKeyName, lpString, (const char*)ini_path.c_str());
    return WriteProfileString ((const char*)ini_path.c_str(), section, lpKeyName, lpString);

    //return 0;
}

int Ini_Unit::Read (const char* section, const char* lpKeyName, const char* lpDefault,
		                        char* lpReturnedString, unsigned short nSize)
{
	//return ::GetProfileString ((const char*)ini_path, lpKeyName, lpDefault,
		               // lpReturnedString, nSize);
	//return ::GetPrivateProfileString ("checksum_generate", lpKeyName, NULL,
	//	                lpReturnedString, nSize, (const char*)ini_path.c_str());

    return ReadProfileString((const char*)ini_path.c_str(), section, lpKeyName, NULL, lpReturnedString, nSize);
    //return 0;
}

int Ini_Unit::WriteProfileString (const char* path, const char* section, 
                                    const char* lpKeyName, const char* lpString)
{
    int retcode = 0;
    char line[LINE_LEN];
    char temp_section[64];
    bool find_section = false, 
         find_key = false;
    char *writeBuf, *readBuf;
    unsigned int file_size = 0;
    //unsigned int write_size = 0;

    FILE *file = fopen(path, "r");
    if(file == NULL) 
    {
        // create it
        char temp_buf[LINE_LEN];
        file = fopen(path, "w");
        if(file == NULL) return -1;

        sprintf(temp_section, "[%s]\n", section);
        sprintf(line, "%s = %s\n", lpKeyName, lpString);
        sprintf(temp_buf, "%s%s", temp_section, line);
        fwrite(temp_buf, 1, strlen(temp_buf), file);
        fclose(file);

        //system("pause");

        return 0;
    }

    fseek(file, 0, SEEK_END);
	file_size = ftell(file);
    fseek(file, 0, 0);

    //if(file_size == 0)
    //{
    //   file_size = 1024 * 1024 * sizeof(char);
    //}
    //else
    //{
    //    file_size += 1024 * 1024 * sizeof(char); // for add new
    //}

    //file_size += 1024 * 1024 * sizeof(char); // for add new
    file_size += 1024 * sizeof(char);   // 1k maybe enough for add new items


    // Profile always be small, maybe we needn't use temp file
    writeBuf = (char *)malloc(file_size);
    readBuf = (char *)malloc(1024 * sizeof(char));
    if(writeBuf == NULL || readBuf == NULL) 
    {
        retcode = -2;
        goto errorret;
    }

    //memset(writeBuf, 0, 1024 * 1024 * sizeof(char));
    memset(writeBuf, 0, file_size);
    memset(readBuf, 0, 1024 * sizeof(char));

    fseek(file, 0, 0);
    sprintf(temp_section, "[%s]", section);
    while( fgets(line, LINE_LEN, file) )
    {
        sprintf(writeBuf, "%s%s", writeBuf, line);
        if(line[0] == ';')
        {
            continue;
        }

        if(strstr(line, temp_section) != NULL)
        {
            find_section = true;
            break;
        }
    }
    if(find_section == false)
    {
        //retcode = -3;
        //goto errorret;
        sprintf(writeBuf, "%s%s\n", writeBuf, temp_section);
    }

    while( fgets(line, LINE_LEN, file) )
    {
        if(line[0] == ';')
        {
            sprintf(writeBuf, "%s%s", writeBuf, line);
            continue;
        }

        // Can't find in this section, add it before the next section.
        // But be careful, the keyname must not have "["
        //if(strstr(line, "[") != NULL)
        if(line[0] == '[')
        {
            break;
        }

        if(strstr(line, lpKeyName) != NULL)
        {
            find_key = true;
            break;
        }

        sprintf(writeBuf, "%s%s", writeBuf, line);
    }
    if(find_key == false) 
    {
        //fclose(file);
        //return -4;
     
        // if didn't find, add it
    }

    //fseek(file, seek_pos, SEEK_SET);

    sprintf(writeBuf, "%s%s = %s\n", writeBuf, lpKeyName, lpString);
    while( fread(readBuf, 1, 1024 - 1, file) )
    {
        sprintf(writeBuf, "%s%s", writeBuf, readBuf);
        //write_size += strlen(readBuf);
    }
    fclose(file);

    // Destroy the old and create new
    file = fopen(path, "w");
    if(file == NULL)
    {
        retcode = -5;
        goto errorret;
    }
    fwrite(writeBuf, 1, strlen(writeBuf), file);


errorret:
    if(readBuf) free(readBuf);
    if(writeBuf) free(writeBuf);
    if(file) fclose(file);

    return retcode;
}


int Ini_Unit::ReadProfileString (const char* path, const char* section,
                                    const char* lpKeyName, const char* lpDefault,
		                                char* lpReturnedString, unsigned short nSize)
{
    int retcode = 0;
    char line[LINE_LEN];
    char temp_section[50];
    bool find_section = false, 
         find_key = false;
    //char *writeBuf, *readBuf;

    FILE *file = fopen(path, "r");
    if(file == NULL) return -1;

    //writeBuf = (char *)malloc(1024 * 1024 * sizeof(char));
    //readBuf = (char *)malloc(1024 * sizeof(char));
    //if(writeBuf == NULL || readBuf == NULL) return -1;
    //memset(writeBuf, 0, 1024 * 1024 * sizeof(char));
    //memset(readBuf, 0, 1024 * sizeof(char));

    //fseek(file, 0, 0);

    sprintf(temp_section, "[%s]", section);
    while( fgets(line, LINE_LEN, file) )
    {
        if(line[0] == ';')
        {
            continue;
        }

        if(strstr(line, temp_section) != NULL)
        {
            find_section = true;
            break;
        }
    }
    if(find_section == false)
    {
        //fclose(file);
        //return -2;
        retcode = -2;
        goto errorret;
    }

    while( fgets(line, LINE_LEN, file) )
    {
        if(line[0] == ';')
        {
            continue;
        }

        //if(strstr(line, "[") != NULL)
        if(line[0] == '[')
        {
            break;
        }

        if(strstr(line, lpKeyName) != NULL)
        {
            find_key = true;
            break;
        }        
    }

    //fclose(file);

    if(find_key == false) 
    {
        if(lpDefault != NULL)
            strncpy(lpReturnedString, lpDefault, nSize);
        else
        {
            //fclose(file);
            //return -3;
            retcode = -3;
            goto errorret;
        }
    }
    else
    {
        //strncpy(lpReturnedString, strstr(line, "=") + 1, nSize);
        //retcode = StrCopyWithoutSpace(lpReturnedString, strstr(line, "=") + 1, nSize);
        StrCopyWithoutSpace(lpReturnedString, strstr(line, "=") + 1, nSize);
    }

errorret:

    //if(readBuf) free(readBuf);
    //if(writeBuf) free(writeBuf);
    fclose(file);

    return retcode;
}

int Ini_Unit::StrCopyWithoutSpace(char *out, const char *src, unsigned int n)
{
    int i = 0, j = 0;
    if(src == NULL || out == NULL) return -1;

    while( src[i] )
    {
        if(src[i] != ' ' && src[i] != '\t' && src[i] != '\n')
        {
            out[j] = src[i];
            j ++;
        }
        if(j == n + 1)
        {
            out[j] = '\0';
            return n;
        }
        i ++;
    }
    out[j] = '\0';

    return j - 1;
}

