/* vi: set sw=4 ts=4: */
/*
 * Utility routines.
 *
 * Copyright (C) 1999-2004 by Erik Andersen <andersen@codepoet.org>
 *
 * Licensed under GPLv2 or later, see file LICENSE in this source tree.
 */

#include "libbb.h"

const char bb_msg_memory_exhausted_mtk[] = "memory exhausted";

FILE* fopen_or_warn(const char *path, const char *mode)
{
	FILE *fp = fopen(path, mode);
	if (!fp) {
		bb_simple_perror_msg(path);
		//errno = 0; /* why? */
	}
	return fp;
}

FILE* fopen_for_read(const char *path)
{
	return fopen(path, "r");
}

FILE* xfopen_for_read(const char *path)
{
	return xfopen(path, "r");
}

FILE* fopen_for_write(const char *path)
{
	return fopen(path, "w");
}

FILE* xfopen_for_write(const char *path)
{
	return xfopen(path, "w");
}

static FILE* xfdopen_helper(unsigned fd_and_rw_bit)
{
	FILE* fp = fdopen(fd_and_rw_bit >> 1, fd_and_rw_bit & 1 ? "w" : "r");
	if (!fp)
		bb_error_msg_and_die(bb_msg_memory_exhausted_mtk);
	return fp;
}
FILE* xfdopen_for_read(int fd)
{
	return xfdopen_helper(fd << 1);
}
FILE* xfdopen_for_write(int fd)
{
	return xfdopen_helper((fd << 1) + 1);
}
