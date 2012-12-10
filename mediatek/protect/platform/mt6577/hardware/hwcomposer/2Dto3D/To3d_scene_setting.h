#ifndef _TO3D_SCENE_SETTING_H_
#define _TO3D_SCENE_SETTING_H_
/*
	define global variables
*/

#define OBJ_CORD_X 31		//object X coordinate (-OBJ_CORD_X, +OBJ_CORD_X)//31
#define OBJ_CORD_Y 18.5		//object Y coordinate (-OBJ_CORD_Y, +OBJ_CORD_Y)//18.5
#define OBJ_CORD_Z 32	    //object Z coordinate 
#define VIEW_ANGLE 60.0		//perspective viewing angle //60

/*
	letterbox
*/
#define LETTERBOX_INTENSITY 0 //letterbox depth [0,255]
//#define LETTERBOX_THR	20	//threshold of lettex box [0,40] (pixels in [0,4]bins)
/*
	disparity
*/
#define Z_NEAR_OFFSET 11.0	//maximum near offset
#define Z_FAR_OFFSET -32.0	//maximum far offset

/*
	detect global model, global & local weighting,  two global weighting
*/
//#define HORI_FLAT_INTERVAL 2	//global and local model transition step [0,32]
//#define TWO_GLOBAL_INTERVAL 2 //two global model transition step [0,32]
//#define INIT_CURRENT_GLOBAL	16 //int new global model weighting [0,32]
//#define CONFIDENCE_FRAMES 16  //confidence threshold of new horizontal line 

/*
	inverse
*/
//#define DIVERSITY_THR1	37037 //inverse depends on APL
//#define DIVERSITY_THR2  88888 //inverse depends on maximal bin of histogram
//#define HISTOGRAM_MAX_THR  12.5
//#define APL_THR 80

#endif
