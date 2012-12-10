/* vi: set sw=4 ts=4: */
/*
 * Copyright (C) 1999-2004 by Erik Andersen <andersen@codepoet.org>
 *
 * Licensed under GPLv2 or later, see file LICENSE in this source tree.
 */

#include "libbb.h"

/* allow default system PATH to be extended via CFLAGS */
#ifndef BB_ADDITIONAL_PATH
#define BB_ADDITIONAL_PATH ""
#endif

/* allow version to be extended, via CFLAGS */
#ifndef BB_EXTRA_VERSION
#define BB_EXTRA_VERSION BB_BT
#endif

//#define BANNER "BusyBox v" BB_VER " (" BB_EXTRA_VERSION ")"
#define BANNER "Busybox v"
const char bb_banner[]  = BANNER;


const char bb_msg_memory_exhausted[]  = "memory exhausted";
const char bb_msg_invalid_date[]  = "invalid date '%s'";
const char bb_msg_unknown[]  = "(unknown)";
const char bb_msg_can_not_create_raw_socket[]  = "can't create raw socket";
const char bb_msg_perm_denied_are_you_root[]  = "permission denied (are you root?)";
const char bb_msg_you_must_be_root[]  = "you must be root";
const char bb_msg_requires_arg[]  = "%s requires an argument";
const char bb_msg_invalid_arg[]  = "invalid argument '%s' to '%s'";
const char bb_msg_standard_input[]  = "standard input";
const char bb_msg_standard_output[]  = "standard output";

const char bb_hexdigits_upcase[]  = "0123456789ABCDEF";

//const char bb_busybox_exec_path[]  = CONFIG_BUSYBOX_EXEC_PATH;

const char bb_default_login_shell[]  = LIBBB_DEFAULT_LOGIN_SHELL;
/* util-linux manpage says /sbin:/bin:/usr/sbin:/usr/bin,
 * but I want to save a few bytes here. Check libbb.h before changing! */
const char bb_PATH_root_path[]  =
	"PATH=/sbin:/usr/sbin:/bin:/usr/bin" BB_ADDITIONAL_PATH;


const int const_int_1 = 1;
/* explicitly = 0, otherwise gcc may make it a common variable
 * and it will end up in bss */
const int const_int_0 = 0;

#include <utmp.h>
/* This is usually something like "/var/adm/wtmp" or "/var/log/wtmp" */
const char bb_path_wtmp_file[]  =
#if defined _PATH_WTMP
	_PATH_WTMP;
#elif defined WTMP_FILE
	WTMP_FILE;
#else
#error unknown path to wtmp file
#endif

/* We use it for "global" data via *(struct global*)&bb_common_bufsiz1.
 * Since gcc insists on aligning struct global's members, it would be a pity
 * (and an alignment fault on some CPUs) to mess it up. */
char bb_common_bufsiz1[COMMON_BUFSIZE] ALIGNED(sizeof(long long));
