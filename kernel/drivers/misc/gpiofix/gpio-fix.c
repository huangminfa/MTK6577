/*
 * gpio_eint - GPIO and EINT module for THL W1
 *
 * hooking taken from "n - for testing kernel function hooking" by Nothize
 * require symsearch module by Skrilaz
 *
 * Copyright (C) 2013 Tanguy Pruvot
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

#include <linux/module.h>
#include <linux/device.h>
#include <linux/earlysuspend.h>
#include <linux/proc_fs.h>
#include <linux/vmalloc.h>
#include <asm/uaccess.h>

#include "hook.h"

//#include <mach/mt_gpio.h>
//#include <mach/eint.h>

#define MODULE_TAG "gpio_eint"
#define PROC_ENTRY

#define BUF_SIZE 16
static char *buf;

static int hooks_count = 0;

static bool hooked = false;


/* Hooked Function */
static void mt65xx_eint_mask(unsigned int eint_num)
{
    printk(KERN_DEBUG MODULE_TAG": %s(%d)\n", __func__, eint_num);

    hooks_count ++;

    /* return */ HOOK_INVOKE(mt65xx_eint_mask, eint_num);
}

/* Hooked Function */
static void mt65xx_eint_unmask(unsigned int eint_num)
{
    printk(KERN_DEBUG MODULE_TAG": %s(%d)\n", __func__, eint_num);

    hooks_count ++;

    /* return */ HOOK_INVOKE(mt65xx_eint_unmask, eint_num);
}


static int proc_hooks_count_read(char *buffer, char **buffer_location,
                                off_t offset, int count, int *eof, void *data) {
    int ret;
    if (offset > 0)
        ret = 0;
    else
        ret = scnprintf(buffer, count, "%u\n", hooks_count);
    return ret;
}

struct hook_info g_hi[] = {
    HOOK_INIT(mt65xx_eint_mask),
    HOOK_INIT(mt65xx_eint_unmask),
    HOOK_INIT_END
};

static int __init gpio_eint_init(void) {
    struct proc_dir_entry *proc_entry;
    int ndx = 0;
    char *name;

    buf = (char *)vmalloc(BUF_SIZE);
    proc_mkdir(MODULE_TAG, NULL);
    proc_entry = create_proc_read_entry(MODULE_TAG"/count", 0444, NULL, proc_hooks_count_read, NULL);

    hook_init();
    hooked = true;

/*
    gpio_dump_regs();

    name = "AST_DATA_INTR\0";
    ndx = get_td_eint_num(name, strlen(name));
    pr_info(MODULE_TAG" %s=%d\n", name, ndx);

    name = "AST_WAKEUP_INTR\0";
    ndx = get_td_eint_num(name, strlen(name));
    pr_info(MODULE_TAG" %s=%d\n", name, ndx);

    name = "AST_RFCONFLICT_INTR\0";
    ndx = get_td_eint_num(name, strlen(name));
    pr_info(MODULE_TAG" %s=%d\n", name, ndx);

*/

    return 0;
}

static void __exit gpio_eint_exit(void) {
    if (hooked) hook_exit();
    remove_proc_entry(MODULE_TAG"/count", NULL);
    remove_proc_entry(MODULE_TAG, NULL);
    vfree(buf);
}

module_init(gpio_eint_init);
module_exit(gpio_eint_exit);

MODULE_ALIAS(MODULE_TAG);
MODULE_VERSION("1.0");
MODULE_DESCRIPTION("Hook to find EINT names");
MODULE_AUTHOR("Tanguy Pruvot");
MODULE_LICENSE("GPL");

