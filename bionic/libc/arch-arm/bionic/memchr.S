#include <machine/cpu-features.h>
#include <machine/asm.h>

ENTRY(memchr)
        LSLS     r3,r1,#24
        LSRS     r3,r3,#24
        CMP      r2,#0
        BEQ      1f
        ADDS     r2,r0,r2
3:
        LDRB     r1,[r0,#0]
        ADDS     r0,r0,#1
        CMP      r0,r2
        BEQ      2f
        CMP      r3,r1
        BNE      3b
2:
        CMP      r3,r1
        BNE      1f
        SUBS     r0,r0,#1
        BX       lr
1:
        MOVS     r0,#0
        BX       lr
        MOVS     r0,r0
END(memchr)

