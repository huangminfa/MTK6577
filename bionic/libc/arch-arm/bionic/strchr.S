#include <machine/cpu-features.h>
#include <machine/asm.h>

ENTRY(strchr)
        LSLS     r2,r1,#24
        LSRS     r2,r2,#24
        SUBS     r0,r0,#1
2:
        LDRB     r1,[r0,#1]
        ADDS     r0,r0,#1
        CMP      r2,r1
        BEQ      1f
        CMP      r1,#0
        BNE      2b
1:
        CMP      r2,r1
        BEQ      3f
        MOVS     r0,#0
3:
        BX       lr
        MOVS     r0,r0
END(strchr)

