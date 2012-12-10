#include <machine/cpu-features.h>
#include <machine/asm.h>

ENTRY(strncmp)
        MOVS     r3,r0
        ORRS     r3,r3,r1
        PUSH     {r4-r6,lr}
        LSLS     r3,r3,#30
        BNE      1f
        LDR      r4,DDD
        LSLS     r5,r4,#7
        B        2f
3:
        ADDS     r0,r0,#4
        ADDS     r1,r1,#4
        SUBS     r2,r2,#4
2:
        CMP      r2,#4
        BCC      1f
        LDR      r3,[r0,#0]
        LDR      r6,[r1,#0]
        PLD(r0, #4)
        PLD(r1, #4)
        CMP      r3,r6
        BNE      1f
        SUBS     r6,r3,r4
        BICS     r6,r6,r3
        TST      r6,r5
        BEQ      3b
1:
        CMP      r2,#0
        BNE      4f
        MOVS     r0,#0
        POP      {r4-r6,pc}
4:
        LDRB     r3,[r0,#0]
        LDRB     r4,[r1,#0]
        ADDS     r0,r0,#1
        ADDS     r1,r1,#1
        CMP      r3,#0
        BEQ      5f
        CMP      r3,r4
        BNE      5f
        SUBS     r2,r2,#1
        BNE      4b
5:
        SUBS     r0,r3,r4
        POP      {r4-r6,pc}
END(strncmp)

DDD:    .word    0x1010101

