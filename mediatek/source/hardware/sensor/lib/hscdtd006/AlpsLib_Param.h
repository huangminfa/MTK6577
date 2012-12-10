/*
 * Copyright (C) 2011 ALPS ELECTRIC CO., LTD. All Rights Reserved.
 * This file must be operated as based on the NDA (Non-Disclosure Agreement)
 * with ALPS ELECTRIC CO., LTD.
 *
 */

#define MAX_LEN            128

/** 
 *  This macro is path of debug file.
 */
#define DEBUG_FILE    "/data/misc/acdapi/ALPSDEBUG"



/** 
 *  This macro excutes saveing and loading calibration file.
 */
#define LOAD_C(VAR)    if ( strstr(buf, #VAR ":") != NULL ) {   \
                           tp = buf + strlen(#VAR) + 1;         \
                           acdapi_clb.VAR = atoi(tp);           \
                           if (flgDebug) {                      \
                               LOGD("acdapi_clb."#VAR " = %d\n", (int)acdapi_clb.VAR);    \
                           }                                    \
                       }

#define SAVE_C(VAR)    snprintf(buf, sizeof buf, "%s:%d\n", #VAR, (int)acdapi_clb.VAR);    \
                       if (fputs(buf, fp) == -1) break;                               \
                       if (flgDebug) {                                                \
                           LOGD("acdapi_clb."#VAR " = %d\n", (int)acdapi_clb.VAR);    \
                       }

//#define CALIB_FILE    "/data/misc/acdapi/calib.dat"
#define CALIB_FILE    "/system/data/misc/acdapi/calib.dat"

#define SAVE_CALIB    SAVE_C(xc3d);    \
                      SAVE_C(yc3d);    \
                      SAVE_C(zc3d);    \
                      SAVE_C(r3d);     \
                      SAVE_C(ax);      \
                      SAVE_C(ay);      \
                      SAVE_C(az);      \
                      SAVE_C(sgm);     \
                      SAVE_C(Acnt);    \
                      SAVE_C(lvl)

#define LOAD_CALIB    LOAD_C(xc3d);    \
                      LOAD_C(yc3d);    \
                      LOAD_C(zc3d);    \
                      LOAD_C(r3d);     \
                      LOAD_C(ax);      \
                      LOAD_C(ay);      \
                      LOAD_C(az);      \
                      LOAD_C(sgm);     \
                      LOAD_C(Acnt);    \
                      LOAD_C(lvl)


/** 
 *  This macro excutes loading parameter file.
 */
#define LOAD_P(VAR)    if ( strstr(buf, #VAR ":") != NULL ) {    \
                           tp = buf + strlen(#VAR) + 1;          \
                           acdapi_prm.VAR = atoi(tp);            \
                           if (flgDebug) {                       \
                               LOGD("acdapi_prm."#VAR " = %d\n", (int)acdapi_prm.VAR);    \
                           }                                     \
                       }

//#define PARAM_FILE    "/data/misc/acdapi/param.dat"
#define PARAM_FILE    "/system/data/misc/acdapi/param.dat"
#define LOAD_PARAM    LOAD_P(gblMagPeriod);                \
                      LOAD_P(flgWRT);                      \
                      LOAD_P(snsAtt);                      \
                      LOAD_P(flgAft);                      \
                      LOAD_P(aftMtx0);                     \
                      LOAD_P(aftMtx1);                     \
                      LOAD_P(aftMtx2);                     \
                      LOAD_P(aftMtx3);                     \
                      LOAD_P(aftMtx4);                     \
                      LOAD_P(aftMtx5);                     \
                      LOAD_P(aftMtx6);                     \
                      LOAD_P(aftMtx7);                     \
                      LOAD_P(aftMtx8);                     \
                      LOAD_P(cl3Mode);                     \
                      LOAD_P(cl3MinLen);                   \
                      LOAD_P(cl3PltLen);                   \
                      LOAD_P(cl3spwPltLen);                \
                      LOAD_P(cl3MaxInCnt);                 \
                      LOAD_P(cl3MinNum0);                  \
                      LOAD_P(cl3spwMaxNum2);               \
                      LOAD_P(cl3spwCnt2);                  \
                      LOAD_P(cl3spwMaxCnt2);               \
                      LOAD_P(cl3spwAreaCnt2);              \
                      LOAD_P(cl3spwRWidth2);               \
                      LOAD_P(cl3spwMaxNum3);               \
                      LOAD_P(cl3spwCnt3);                  \
                      LOAD_P(cl3spwMaxCnt3);               \
                      LOAD_P(cl3spwAreaCnt3);              \
                      LOAD_P(cl3spwRWidth3);               \
                      LOAD_P(cl3spwAreaPriority);          \
                      LOAD_P(cl3spwMaxVar1);               \
                      LOAD_P(cl3spwMaxVar2);               \
                      LOAD_P(cl3spwMaxVar3);               \
                      LOAD_P(dr3ValidL);                   \
                      LOAD_P(dr3RRange);                   \
                      LOAD_P(dr3NRange);                   \
                      LOAD_P(dr3StaWidth);                 \
                      LOAD_P(dr3StaTimes);                 \
                      LOAD_P(dr3StaCover);                 \
                      LOAD_P(dr3AutoClb);                  \
                      LOAD_P(dr3AziAve);                   \
                      LOAD_P(dr3AziAveStop);               \
                      LOAD_P(dr3MagAvLen);                 \
                      LOAD_P(dr3AccAvLen);                 \
                      LOAD_P(dr3AttAveLen);                \
                      LOAD_P(dr3AccZLimit);                \
                      LOAD_P(dr3AttLimit);                 \
                      LOAD_P(dr3CalAttType);               \
                      LOAD_P(dr3AttMode);                  \
                      LOAD_P(dr3AccStopSnsThr);            \
                      LOAD_P(dr3AccStopSnsCnt);            \
                      LOAD_P(dr3SmtPeriod);                \
                      LOAD_P(dr3SmtStep);                  \
                      LOAD_P(dr3thetaNtoR);                \
                      LOAD_P(dr3thetaRtoN);                \
                      LOAD_P(dr3AndRev);                   \
                      LOAD_P(dr3FilterNoInit);             \
                      LOAD_P(incAutoStart);                \
                      LOAD_P(incCont);                     \
                      LOAD_P(incLvl);                      \
                      LOAD_P(incCntNum);                   \
                      LOAD_P(incSlntNum);                  \
                      LOAD_P(incIncJudgR);                 \
                      LOAD_P(incAccThr);                   \
                      LOAD_P(incMaxCnt);                   \
                      LOAD_P(incMagAvLen);                 \
                      LOAD_P(incAccAvLen);

#define LOAD_PARAM_GYR    LOAD_P(gyrTimes);                \
                          LOAD_P(gyrPltLen);               \
                          LOAD_P(gyrPltNum);               \
                          LOAD_P(gyrMinLen);               \
                          LOAD_P(gyrMinCal);               \
                          LOAD_P(gyrMinCalAcc);            \
                          LOAD_P(gyrRRange);               \
                          LOAD_P(gyrAutoClb);              \
                          LOAD_P(gyrBufNum);               \
                          LOAD_P(gyrArcMinVn);             \
                          LOAD_P(gyrArcMaxVt);             \
                          LOAD_P(gyrSilPltN);              \
                          LOAD_P(gyrSilLenDvaL);           \
                          LOAD_P(gyrSilLenDvaH);           \
                          LOAD_P(gyrSilAccN);              \
                          LOAD_P(gyrSilAccL);              \
                          LOAD_P(gyrMkSrtCir);             \
                          LOAD_P(gyrCirStyN);              \
                          LOAD_P(gyrCirStyT);              \
                          LOAD_P(gyrSPolLimT);             \
                          LOAD_P(gyrAccSuiT);              \
                          LOAD_P(gyrAccSuiN);              \
                          LOAD_P(gyrAccSuiP);              \
                          LOAD_P(gyrZureTimes);            \
                          LOAD_P(gyrZureSize);             \
                          LOAD_P(gyrZureDist);             \
                          LOAD_P(gyrFixNum);               \
                          LOAD_P(gyrMagEna);               \
                          LOAD_P(gyrMagAvLen);             \
                          LOAD_P(gyrAccEna);               \
                          LOAD_P(gyrAccAvLen);             \
                          LOAD_P(gyrGyFltEna);             \
                          LOAD_P(gyrGyFltAvLen);           \
                          LOAD_P(gyrAziFltEna);            \
                          LOAD_P(gyrAziFltAvLen);          \
                          LOAD_P(gyrAziFrqEna);            \
                          LOAD_P(gyrAziFrqLen);            \
                          LOAD_P(gyrMinLen2);              \
                          LOAD_P(gyrMinData);              \
                          LOAD_P(gyrlenAddMax);            \
                          LOAD_P(gyrStaSum);               \
                          LOAD_P(gyrStaMax);               \
                          LOAD_P(gyrRpdSum);               \
                          LOAD_P(gyrRpdMax);


/** 
 *  This macro excutes loading calibration parameter.
 */
#define LOAD_P_CAL(VAR, TYPE, TIME, DEF)    fseek(fp, 0, SEEK_SET);                                            \
                                            acdapi_prm_cal.TYPE##_t##TIME.VAR = DEF;                           \
                                            if (flgDebug) {                                                    \
                                                LOGD("acdapi_prm_cal."#TYPE "_t" #TIME"." #VAR " = %d\n", acdapi_prm_cal.TYPE##_t##TIME.VAR);    \
                                            }                                                                  \
                                            while (fgets(buf, MAX_LEN, fp) != NULL) {                          \
                                                if ( strstr(buf, #VAR "." #TYPE ".t" #TIME ":") != NULL ) {    \
                                                    tp = buf + strlen(#VAR "." #TYPE ".t" #TIME) + 1;          \
                                                    acdapi_prm_cal.TYPE##_t##TIME.VAR = atoi(tp);              \
                                                    if (flgDebug) {                                            \
                                                        LOGD("acdapi_prm_cal."#TYPE "_t" #TIME"." #VAR " = %d\n", acdapi_prm_cal.TYPE##_t##TIME.VAR);    \
                                                    }                                                          \
                                                }                                                              \
                                            }

#define LOAD_PARAM_CAL_ORI    LOAD_P_CAL(cl3Mode,        ori,  10,     5);    \
                              LOAD_P_CAL(cl3MinLen,      ori,  10,    30);    \
                              LOAD_P_CAL(cl3MaxInCnt,    ori,  10, 10000);    \
                              LOAD_P_CAL(cl3MinNum0,     ori,  10,   100);    \
                              LOAD_P_CAL(cl3spwMaxNum2,  ori,  10,    30);    \
                              LOAD_P_CAL(cl3spwCnt2,     ori,  10,   100);    \
                              LOAD_P_CAL(cl3spwMaxCnt2,  ori,  10,   200);    \
                              LOAD_P_CAL(cl3spwAreaCnt2, ori,  10,    10);    \
                              LOAD_P_CAL(cl3spwRWidth2,  ori,  10,    50);    \
                              LOAD_P_CAL(cl3spwMaxNum3,  ori,  10,    30);    \
                              LOAD_P_CAL(cl3spwCnt3,     ori,  10,   200);    \
                              LOAD_P_CAL(cl3spwMaxCnt3,  ori,  10,   300);    \
                              LOAD_P_CAL(cl3spwAreaCnt3, ori,  10,    14);    \
                              LOAD_P_CAL(cl3spwRWidth3,  ori,  10,    50);    \
                              LOAD_P_CAL(dr3MagAvLen,    ori,  10,    32);    \
                              LOAD_P_CAL(dr3AccAvLen,    ori,  10,    32);    \
                              LOAD_P_CAL(dr3AziAve,      ori,  10,    96);    \
                                                                              \
                              LOAD_P_CAL(cl3Mode,        ori,  20,     5);    \
                              LOAD_P_CAL(cl3MinLen,      ori,  20,    60);    \
                              LOAD_P_CAL(cl3MaxInCnt,    ori,  20,   500);    \
                              LOAD_P_CAL(cl3MinNum0,     ori,  20,    50);    \
                              LOAD_P_CAL(cl3spwMaxNum2,  ori,  20,    15);    \
                              LOAD_P_CAL(cl3spwCnt2,     ori,  20,    50);    \
                              LOAD_P_CAL(cl3spwMaxCnt2,  ori,  20,   100);    \
                              LOAD_P_CAL(cl3spwAreaCnt2, ori,  20,    10);    \
                              LOAD_P_CAL(cl3spwRWidth2,  ori,  20,    50);    \
                              LOAD_P_CAL(cl3spwMaxNum3,  ori,  20,    15);    \
                              LOAD_P_CAL(cl3spwCnt3,     ori,  20,   100);    \
                              LOAD_P_CAL(cl3spwMaxCnt3,  ori,  20,   150);    \
                              LOAD_P_CAL(cl3spwAreaCnt3, ori,  20,    14);    \
                              LOAD_P_CAL(cl3spwRWidth3,  ori,  20,    50);    \
                              LOAD_P_CAL(dr3MagAvLen,    ori,  20,    16);    \
                              LOAD_P_CAL(dr3AccAvLen,    ori,  20,    16);    \
                              LOAD_P_CAL(dr3AziAve,      ori,  20,    70);    \
                                                                              \
                              LOAD_P_CAL(cl3Mode,        ori,  50,     5);    \
                              LOAD_P_CAL(cl3MinLen,      ori,  50,    60);    \
                              LOAD_P_CAL(cl3MaxInCnt,    ori,  50,   200);    \
                              LOAD_P_CAL(cl3MinNum0,     ori,  50,    30);    \
                              LOAD_P_CAL(cl3spwMaxNum2,  ori,  50,     8);    \
                              LOAD_P_CAL(cl3spwCnt2,     ori,  50,    30);    \
                              LOAD_P_CAL(cl3spwMaxCnt2,  ori,  50,    40);    \
                              LOAD_P_CAL(cl3spwAreaCnt2, ori,  50,    10);    \
                              LOAD_P_CAL(cl3spwRWidth2,  ori,  50,    50);    \
                              LOAD_P_CAL(cl3spwMaxNum3,  ori,  50,     8);    \
                              LOAD_P_CAL(cl3spwCnt3,     ori,  50,    40);    \
                              LOAD_P_CAL(cl3spwMaxCnt3,  ori,  50,    60);    \
                              LOAD_P_CAL(cl3spwAreaCnt3, ori,  50,    14);    \
                              LOAD_P_CAL(cl3spwRWidth3,  ori,  50,    50);    \
                              LOAD_P_CAL(dr3MagAvLen,    ori,  50,    10);    \
                              LOAD_P_CAL(dr3AccAvLen,    ori,  50,    10);    \
                              LOAD_P_CAL(dr3AziAve,      ori,  50,    50);    \
                                                                              \
                              LOAD_P_CAL(cl3Mode,        ori, 100,     6);    \
                              LOAD_P_CAL(cl3MinLen,      ori, 100,    60);    \
                              LOAD_P_CAL(cl3MaxInCnt,    ori, 100,   100);    \
                              LOAD_P_CAL(cl3MinNum0,     ori, 100,    30);    \
                              LOAD_P_CAL(cl3spwMaxNum2,  ori, 100,     8);    \
                              LOAD_P_CAL(cl3spwCnt2,     ori, 100,    30);    \
                              LOAD_P_CAL(cl3spwMaxCnt2,  ori, 100,    50);    \
                              LOAD_P_CAL(cl3spwAreaCnt2, ori, 100,    10);    \
                              LOAD_P_CAL(cl3spwRWidth2,  ori, 100,    25);    \
                              LOAD_P_CAL(cl3spwMaxNum3,  ori, 100,     8);    \
                              LOAD_P_CAL(cl3spwCnt3,     ori, 100,    50);    \
                              LOAD_P_CAL(cl3spwMaxCnt3,  ori, 100,    60);    \
                              LOAD_P_CAL(cl3spwAreaCnt3, ori, 100,    16);    \
                              LOAD_P_CAL(cl3spwRWidth3,  ori, 100,    25);    \
                              LOAD_P_CAL(dr3MagAvLen,    ori, 100,     5);    \
                              LOAD_P_CAL(dr3AccAvLen,    ori, 100,     5);    \
                              LOAD_P_CAL(dr3AziAve,      ori, 100,    30);

#define LOAD_PARAM_CAL_GYR    LOAD_P_CAL(cl3Mode,        gyr,  10,     6);    \
                              LOAD_P_CAL(cl3MinLen,      gyr,  10,    30);    \
                              LOAD_P_CAL(cl3MaxInCnt,    gyr,  10, 10000);    \
                              LOAD_P_CAL(cl3MinNum0,     gyr,  10,   150);    \
                              LOAD_P_CAL(cl3spwMaxNum2,  gyr,  10,    30);    \
                              LOAD_P_CAL(cl3spwCnt2,     gyr,  10,   150);    \
                              LOAD_P_CAL(cl3spwMaxCnt2,  gyr,  10,   300);    \
                              LOAD_P_CAL(cl3spwAreaCnt2, gyr,  10,    10);    \
                              LOAD_P_CAL(cl3spwRWidth2,  gyr,  10,    25);    \
                              LOAD_P_CAL(cl3spwMaxNum3,  gyr,  10,    30);    \
                              LOAD_P_CAL(cl3spwCnt3,     gyr,  10,   300);    \
                              LOAD_P_CAL(cl3spwMaxCnt3,  gyr,  10,   500);    \
                              LOAD_P_CAL(cl3spwAreaCnt3, gyr,  10,    16);    \
                              LOAD_P_CAL(cl3spwRWidth3,  gyr,  10,    25);    \
                              LOAD_P_CAL(dr3MagAvLen,    gyr,  10,    32);    \
                              LOAD_P_CAL(dr3AccAvLen,    gyr,  10,    32);    \
                              LOAD_P_CAL(dr3AziAve,      gyr,  10,    96);


#define SET_P_CAL(VAR, TYPE, TIME)   acdapi_prm.VAR = acdapi_prm_cal.TYPE##_t##TIME.VAR;    \
                                     if (flgDebug) {                                        \
                                         LOGD("acdapi_prm." #VAR " = %d\n", acdapi_prm_cal.TYPE##_t##TIME.VAR);    \
                                     }

#define SET_PARAM_CAL(TYPE, TIME)    acdapi_prm.gblMagPeriod = TIME;            \
                                     SET_P_CAL(cl3Mode,        TYPE,  TIME);    \
                                     SET_P_CAL(cl3MinLen,      TYPE,  TIME);    \
                                     SET_P_CAL(cl3MaxInCnt,    TYPE,  TIME);    \
                                     SET_P_CAL(cl3MinNum0,     TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwMaxNum2,  TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwCnt2,     TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwMaxCnt2,  TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwAreaCnt2, TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwRWidth2,  TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwMaxNum3,  TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwCnt3,     TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwMaxCnt3,  TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwAreaCnt3, TYPE,  TIME);    \
                                     SET_P_CAL(cl3spwRWidth3,  TYPE,  TIME);    \
                                     SET_P_CAL(dr3MagAvLen,    TYPE,  TIME);    \
                                     SET_P_CAL(dr3AccAvLen,    TYPE,  TIME);    \
                                     SET_P_CAL(dr3AziAve,      TYPE,  TIME);

/** 
 *  This macro excutes loading sensors parameter file.
 */
#define    LOAD_S(VAR)    if ( strstr(buf, #VAR ":") != NULL ) {    \
                              tp = buf + strlen(#VAR) + 1;          \
                              VAR = atoi(tp);                       \
                              if (flgDebug) {                       \
                                  LOGD(#VAR " = %d\n", VAR);        \
                              }                                     \
                          }

//#define SENSOES_FILE    "/data/misc/acdapi/sensors.dat"
#define SENSOES_FILE    "/system/data/misc/acdapi/sensors.dat"
#define LOAD_SENSORS    LOAD_S(acc_Off_X);        \
                        LOAD_S(acc_Off_Y);        \
                        LOAD_S(acc_Off_Z);

/*****************************************************************************/

