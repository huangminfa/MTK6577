#include <machine/cpu-features.h>
#include <machine/asm.h>

ENTRY(strlcpy)
        PUSH     {r4-r7,lr}
        MOV      r12,r1
        ADDS     r3,r0,r2
        MOVS     r2,r0
        LDR      r4,DDD
        ORRS     r2,r2,r1
        SUBS     r3,r3,#1
        LSLS     r2,r2,#30
        BNE      1f
        LSLS     r6,r4,#7
        B        2f
4:
        STM      r0!,{r2}
        ADDS     r1,r1,#4
2:
        ADDS     r5,r0,#4
        LDR      r2,[r1,#0]
        CMP      r5,r3
        BHI      3f
        SUBS     r5,r2,r4
        BICS     r5,r5,r2
        TST      r5,r6
        BEQ      4b
        B        3f
5:
        ADDS     r1,r1,#4
3:
        LDR      r5,[r1,#0]
        SUBS     r7,r5,r4
        BICS     r7,r7,r5
        TST      r7,r6
        BEQ      5b
        B        6f
7:
        ADDS     r1,r1,#1
6:
        LDRB     r4,[r1,#0]
        CMP      r4,#0
        BNE      7b
10:
        CMP      r0,r3
        BCC      8f
        CMP      r0,r3
        BNE      9f
        MOVS     r2,#0
        STRB     r2,[r0,#0]
        B        9f
8:
        LSLS     r4,r2,#24
        LSRS     r4,r4,#24
        STRB     r4,[r0,#0]
        ADDS     r0,r0,#1
        CMP      r4,#0
        BEQ      9f
        LSRS     r2,r2,#8
        B        10b
12:
        ADDS     r1,r1,#1
        STRB     r2,[r0,#0]
        ADDS     r0,r0,#1
1:
        CMP      r0,r3
        BCS      11f
        LDRB     r2,[r1,#0]
        CMP      r2,#0
        BNE      12b
11:
        CMP      r0,r3
        BHI      13f
        MOVS     r2,#0
        STRB     r2,[r0,#0]
        B        13f
14:
        ADDS     r1,r1,#1
13:
        LDRB     r0,[r1,#0]
        CMP      r0,#0
        BNE      14b
9:
        MOV      r0,r12
        SUBS     r0,r1,r0
        POP      {r4-r7,pc}
END(strlcpy)

DDD:    .word    0x1010101

