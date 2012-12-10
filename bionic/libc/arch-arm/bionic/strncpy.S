#include <machine/cpu-features.h>
#include <machine/asm.h>

ENTRY(strncpy)
        TST      r0,#3
        PUSH     {r4,lr}
        MOV      r4,r0
        TSTEQ    r1,#3
        BNE      1f
        LDR      lr,DDD
4:
        SUBS     r2,r2,#4
        BLT      2f
        LDR      r3,[r1],#4
        PLD(r1, #0)
        SUB      r12,r3,lr
        BIC      r12,r12,r3
        ANDS     r12,r12,lr,LSL #7
        STREQ    r3,[r0],#4
        BEQ      4b
        SUB      r1,r1,#4
2:
        ADD      r2,r2,#4
1:
        SUBS     r2,r2,#1
        BLT      3f
        LDRB     r3,[r1],#1
        CMP      r3,#0
        STRB     r3,[r0],#1
        BNE      1b
        MOV      r1,r2
        BL       __test_memclr
3:
        MOV      r0,r4
        POP      {r4,lr}
        BX       lr



__test_memclr:
        MOV      r2,#0
        CMP      r1,#4
        BCC      5f
        ANDS     r12,r0,#3
        BEQ      test_memset_w
        RSB      r12,r12,#4
        CMP      r12,#2
        STRNEB   r2,[r0],#1
        SUB      r1,r1,r12
        STRGEH   r2,[r0],#2
        B        test_memset_w
5:
        LSLS     r12,r1,#31
        STRCSB   r2,[r0],#1
        STRCSB   r2,[r0],#1
        STRMIB   r2,[r0],#1
        BX       lr

test_memset_w:
        STMDB    sp!,{lr}
        MOV      r3,r2
        MOV      r12,r2
        MOV      lr,r2
        SUBS     r1,r1,#0x20
6:
        STMCS    r0!,{r2,r3,r12,lr}
        STMCS    r0!,{r2,r3,r12,lr}
        SUBCSS   r1,r1,#0x20
        BCS      6b
        LSLS     r1,r1,#28
        STMCS    r0!,{r2,r3,r12,lr}
        STMMI    r0!,{r2,r3}
        LDM      sp!,{lr}
        LSLS     r1,r1,#2
        STRCS    r2,[r0],#4
        BXEQ     lr
        STRMIH   r2,[r0],#2
        TST      r1,#0x40000000
        STRNEB   r2,[r0],#1
        BX       lr
END(strncpy)

DDD:    .word    0x1010101

