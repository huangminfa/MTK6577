#ifndef _TO3D_CORE_H_
#define _TO3D_CORE_H_
#include "MTKTo3d.h"
#include "MTKTo3dType.h"

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define TO3D_LOG_BYTE_PER_FRAME 96
#define TO3D_LOG_MAX_FRAMES 600


typedef struct{
	//letterbox
	MUINT32 LETTERBOX_THR;		//20	//threshold of lettex box [0,40] (pixels in [0,4]bins)
	//disparity
	MINT32 CROPPING2;			//0  //will effect viewing depth
	//detect global model, global & local weighting,  two global weighting
	MUINT32 HORI_FLAT_INTERVAL; // 2	//global and local model transition step [0,32]
	MUINT32 TWO_GLOBAL_INTERVAL;// 2 //two global model transition step [0,32]
	MUINT32 INIT_CURRENT_GLOBAL;//	16 //int new global model weighting [0,32]
	MUINT32 CONFIDENCE_FRAMES;	// 16  //confidence threshold of new horizontal line 
	//inverse
	MUINT32 DIVERSITY_THR1;		//	37037 //inverse depends on APL
	MUINT32 DIVERSITY_THR2;		//  88888 //inverse depends on maximal bin of histogram
	MUINT32 HISTOGRAM_MAX_THR;  //12
	MUINT32 APL_THR;			// 80
}TO3D_TUNING_PARA2_STRUCT;


typedef struct
{
    MUINT32 ext_mem_start_addr; //working buffer start address
    MUINT32 ext_mem_size;
}TO3D_EXT_MEM_INFO;

typedef enum
{
	TO3D_SCENE_HORIZONTAL,
	TO3D_SCENE_FLAT,	
	NUM_OF_SCENE
}TO3D_SUPPORT_SCENE;

typedef struct
{
	/*
		weighting factor
	*/
	GLint	w_current_global;	//weighting of current global model (used for horizontal line transition)
	GLuint	w_global;			//weighting of global model(fixed 8/32)
	//GLuint	w_horizontal;		//weighting of horizontal mode
	GLint	w_flat;
	//GLuint	w_inverse;			//weighting of local depth map inverse
	//GLuint	w_non_inverse;

	/*
		state info.
	*/
	TO3D_SUPPORT_SCENE	mode;				//scene : flat = 1, horizontal = 0;
	MUINT32	his_max_idx;		//maximal bin of histogram map
	MUINT32	pre_apl;			//previous apl
	GLint	cur_H;				//global model horizontal line position
	GLint	new_H;				//new global model horizontal line position
	GLint	hori;				//current frame horizontal line position
	GLuint	confidence_counter;	//confidence_counter to check new H is stable
	MINT32	final_inverse_flag;	//final inverse flag
	MINT32	scene_change;		//set to 1 if scene changed
	MINT32	letterbox;			//set to 1 if found letterbox
	GLuint	transition_state;	//CID transition state
	//GLuint step;				//CID transition step size
	MINT32 scene_diff;
	MINT32 top_letterbox;
	MINT32 bot_letterbox;
	MINT32 diversity;
	MINT32 max_sad;

}TO3D_ALGO_STATE_MACHINE_STRUCT;

typedef struct
{   
	//input file name
	MINT8*	inputFilename;

	//state machine Loc
	void*	state;
	GLint	w_non_inverse_loc;	//Loc of non inverse weighting
	GLint	w_flat_loc;			//Loc of flat weighting
	GLint	w_global_loc;		//Loc of global model weighting

	//LOG ptr
	MUINT8* p_log;

	//video input
	MUINT8* large_image; 
	MUINT8* small_image;
	// Texture handle
	GLuint	global_texture;		//sad map
	GLuint	local_texture;		//current depth map
	GLuint	source_texture;	//original image
	GLuint	depth_table_texture;//texture index of depth table

	GLuint	idx_num;			//rendering vetex index

	/*
		Histogram computation
	*/
	GLint	ProgramObject_histogram;	//shader of histogram computation
	GLint	ColorLoc_histogram;			//Loc of luma information (can be replaced by VTF) 
	GLint	YPosLoc_histogram;			//position of every row in histogram map
	GLint	histogram_size[2];			//WxH = bin x rows
	GLuint	histogram_texture;			//texture index of hidtogram map
	GLint	bins;						//bins
	GLuint	fbo_his, rbo_his;			//fbo & rbo of histogram map

	/*
	   original image size
	   depth map size (resizer output)
	*/
	GLint	ori_img_size[2];		//original image size
	GLint	depth_map_size[2];		//small image WxH

	/*
	   Rendering
	*/

	GLuint ProgramObject_render;		//shader of rendering
	GLint  PositionLoc_render;			//loc of vertex position
	GLint  TexCoordLoc_render;			//loc of texture coord.
	GLint  LocDepthTexLoc_render;		//loc of local depth texture
	GLint  DepthTabLoc_render;			//loc of depth table
	GLint  GlobalDepthTexLoc_render;	//loc of global depth texture
	GLint  mvpLoc_render;				//loc of projection matrix	
	GLint  SourceLoc_render;			//loc of original image
	GLint  NearOffsetLoc_render;		//loc of z near offset
	GLint  FarOffsetLoc_render;			//loc of z far offset

	/*
		Output side-by-side images (reserved)
	*/
	GLuint renderLR_texture;			//Left side texture (reserved)
	GLuint fbo_renderLR, rbo_renderLR;	//fbo & rbo for left side 
	MUINT8* out_img;
 
} TO3D_GPU_DATA_STRUCT;

/*
	vetex information
*/
typedef struct VTX_FMT{
	//vertex position
	GLfloat x;
	GLfloat y;
	GLfloat z;
	//texture coordinate
	GLfloat u;
	GLfloat v;
} TO3D_VTX_FMT_STRUCT;


MRESULT To3dCoreSetTuningPara2(TO3D_TUNING_PARA2_STRUCT*);
MRESULT To3dCoreInit(TO3D_SET_ENV_INFO_STRUCT*, TO3D_EXT_MEM_INFO*);
MRESULT To3dCoreInitLargeTexture(TO3D_SET_PROC_INFO_STRUCT *);
MRESULT To3dCoreMain();
void To3dCoreGetResult(TO3D_RESULT_STRUCT*);
void To3dCoreSetLogBuffer(MUINT8 *);
void QueryWorkingBufSize(MUINT16 w, MUINT16 h, MUINT32* total_work_buffer, MUINT32* work_buffer);
#endif
