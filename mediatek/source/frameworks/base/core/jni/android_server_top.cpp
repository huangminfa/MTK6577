/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (c) 2008, The Android Open Source Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the 
 *    distribution.
 *  * Neither the name of Google, Inc. nor the names of its contributors
 *    may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

struct cpu_info {
    long unsigned utime, ntime, stime, itime;
    long unsigned iowtime, irqtime, sirqtime;
};

#define PROC_NAME_LEN 64
#define THREAD_NAME_LEN 32
#define DATA_LEN 64


struct proc_info {
    struct proc_info *next;
    pid_t pid;
    pid_t tid;
    uid_t uid;
    gid_t gid;
    char name[PROC_NAME_LEN];
    char tname[THREAD_NAME_LEN];
	char date[DATA_LEN];
	time_t time;
    char state;
    long unsigned utime;
    long unsigned stime;
    long unsigned delta_utime;
    long unsigned delta_stime;
    long unsigned delta_time;
    long vss;
    long rss;
    int num_threads;
    char policy[32];
};

struct proc_list {
    struct proc_info **array;
    int size;
};

char*myitoa(int i, char* buf) ;

#define INIT_PROCS 50
#define THREAD_MULT 8

static pthread_mutex_t anr_lock = PTHREAD_MUTEX_INITIALIZER;
static int TopFileEn = 0;
static int anrFileNum = 0;
static int anr_top_threa_exist = 0;
static char new_procs_date[DATA_LEN];
static time_t new_procs_time;
static struct cpu_info anr_cpu;
static unsigned long anr_total_delta = 0xFFFFFFFF;
static unsigned long normal_total_delta = 0xFFFFFFFF;

static struct proc_info **old_procs, **new_procs,**anr_procs;
static int num_old_procs, num_new_procs,num_anr_procs;
static struct proc_info *free_procs;
static int num_used_procs, num_free_procs;

static int max_procs, delay, iterations, threads;

static struct cpu_info old_cpu, new_cpu;

static struct proc_info *alloc_proc(void);
static void free_proc(struct proc_info *proc);
static int read_procs(void);
static int read_stat(char *filename, struct proc_info *proc);
static void read_policy(int pid, struct proc_info *proc);
static int add_proc(int proc_num, struct proc_info *proc);
static int read_cmdline(char *filename, struct proc_info *proc);
static int read_status(char *filename, struct proc_info *proc);
static int print_procs(FILE*f);
static struct proc_info *find_old_proc(pid_t pid, pid_t tid);
static void free_old_procs(void);
static int (*proc_cmp)(const void *a, const void *b);
static int proc_cpu_cmp(const void *a, const void *b);
static int proc_vss_cmp(const void *a, const void *b);
static int proc_rss_cmp(const void *a, const void *b);
static int proc_thr_cmp(const void *a, const void *b);
static int numcmp(long long a, long long b);
static void usage(char *cmd);

//#define TOP1_CMD_PRIO 80
#define TOP1_CMD_PRIO 40

static struct proc_info *find_anr_proc(pid_t pid, pid_t tid) {
    int i;

    for (i = 0; i < num_new_procs; i++)
        if (new_procs[i] && (new_procs[i]->pid == pid) && (new_procs[i]->tid == tid))
            return new_procs[i];

    return NULL;
}

static int print_anr_procs(FILE *f) 
{
    int i;
    struct proc_info *old_proc, *proc;
    long unsigned total_delta_time;
    struct passwd *user;
    struct group *group;
    char *user_str, user_buf[20];
    char *group_str, group_buf[20];

	if(!anr_procs || !num_anr_procs) return -1;
//print out when the anr procs were created and when the new_procs were created
	if(f!=NULL && anr_procs[0]!=NULL){
		fprintf (f, "\n\n anr procs dump time: ") ;
		fprintf (f, "%s",anr_procs[0]->date);
		fprintf (f, "\n\n") ;
	}else if(anr_procs[0]!=NULL){
		LOGD("\n\n anr procs dump time: %s",anr_procs[0]->date) ;
		LOGD("\n\n") ;
	}
    for (i = 0; i < num_anr_procs; i++) {
        if (anr_procs[i]) {
            old_proc = find_anr_proc(anr_procs[i]->pid, anr_procs[i]->tid);
            if (old_proc) {
                anr_procs[i]->delta_utime = anr_procs[i]->utime - old_proc->utime;
                anr_procs[i]->delta_stime = anr_procs[i]->stime - old_proc->stime;
            } else {
                anr_procs[i]->delta_utime = 0;
                anr_procs[i]->delta_stime = 0;
            }
            anr_procs[i]->delta_time = anr_procs[i]->delta_utime + anr_procs[i]->delta_stime;
        }
    }

    qsort(anr_procs, num_anr_procs, sizeof(struct proc_info *), proc_cmp);

   	if (!threads) 
        if(f!=NULL)fprintf(f, "%5s %4s %4s%% %1s %5s %7s %7s %3s %-8s %s\n", "PID", "Jeffies","CPU", "S", "#THR", "VSS", "RSS", "PCY", "UID", "Name");
		else LOGD("%5s %4s %4s%% %1s %5s %7s %7s %3s %-8s %s\n", "PID", "Jeffies","CPU", "S", "#THR", "VSS", "RSS", "PCY", "UID", "Name");
   	 else
       	 if(f!=NULL) fprintf(f, "%5s %5s %4s %4s%% %1s %7s %7s %3s %-8s %-15s %s\n", "PID", "TID","Jeffies" ,"CPU", "S", "VSS", "RSS", "PCY", "UID", "Thread", "Proc");
		 else LOGD("%5s %5s %4s %4s%% %1s %7s %7s %3s %-8s %-15s %s\n", "PID", "TID", "Jeffies","CPU", "S", "VSS", "RSS", "PCY", "UID", "Thread", "Proc");
   	 for (i = 0; i < num_anr_procs; i++) {
     	 proc = anr_procs[i];

     if (!proc || (max_procs && (i >= max_procs)))break;
	 
	 user  = getpwuid(proc->uid);
     group = getgrgid(proc->gid);

	 if (user && user->pw_name) {
       	user_str = user->pw_name;
	 } else {
        snprintf(user_buf, 20, "%d", proc->uid);
        user_str = user_buf;
	 }

   	 if (group && group->gr_name) {
         group_str = group->gr_name;
	 } else {
         snprintf(group_buf, 20, "%d", proc->gid);
         group_str = group_buf;
	 }

	 if (!threads) 
         if(f!=NULL)fprintf(f, "%5d %3ld %8ld%% %c %5d %6ldK %6ldK %3s %-8.8s %s\n", proc->pid, proc->delta_time,proc->delta_time*100/anr_total_delta, proc->state, proc->num_threads,
           	    proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->name[0] != 0 ? proc->name : proc->tname);
		 else LOGD("%5d %3ld %8ld%% %c %5d %6ldK %6ldK %3s %-8.8s %s\n", proc->pid, proc->delta_time,proc->delta_time*100/anr_total_delta ,proc->state, proc->num_threads,
           	    proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->name[0] != 0 ? proc->name : proc->tname);
	 else
        if(f!=NULL) fprintf(f, "%5d %5d %3ld %8ld%% %c %6ldK %6ldK %3s %-8.8s %-15s %s\n", proc->pid, proc->tid, proc->delta_time,proc->delta_time*100/anr_total_delta, proc->state,
      	        proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->tname, proc->name);
		else LOGD("%5d %5d %3ld %8ld%% %c %6ldK %6ldK %3s %-8.8s %-15s %s\n", proc->pid, proc->tid, proc->delta_time,proc->delta_time*100/anr_total_delta,proc->state,
      	        proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->tname, proc->name);
       
    }

//print the anr_top_thread record information:
	if(!new_procs || !num_new_procs) return -1;
	if(f!=NULL && new_procs[0]!=NULL){
		fprintf (f, "\n\n new procs dump time: ") ;
		LOGD("!!!!!!!!!!new_procs[0]->data:%s",new_procs_date);
		fprintf (f, "%s",new_procs_date);
		fprintf (f, "\n\n") ;
	}else if(new_procs[0]!=NULL){
		LOGD("\n\n new procs dump time: %s",new_procs_date) ;
		LOGD("\n\n") ;
	}
    
    qsort(new_procs, num_new_procs, sizeof(struct proc_info *), proc_cmp);

   	if (!threads) 
        if(f!=NULL)fprintf(f, "%5s %4s %4s%% %1s %5s %7s %7s %3s %-8s %s\n", "PID", "Jeffies","CPU", "S", "#THR", "VSS", "RSS", "PCY", "UID", "Name");
		else LOGD("%5s %4s %4s%% %1s %5s %7s %7s %3s %-8s %s\n", "PID", "Jeffies","CPU", "S", "#THR", "VSS", "RSS", "PCY", "UID", "Name");
   	 else
       	 if(f!=NULL) fprintf(f, "%5s %5s %4s %4s%% %1s %7s %7s %3s %-8s %-15s %s\n", "PID", "TID", "Jeffies","CPU", "S", "VSS", "RSS", "PCY", "UID", "Thread", "Proc");
		 else LOGD("%5s %5s %4s %4s%% %1s %7s %7s %3s %-8s %-15s %s\n", "PID", "TID", "Jeffies","CPU", "S", "VSS", "RSS", "PCY", "UID", "Thread", "Proc");
   	 for (i = 0; i < num_new_procs; i++) {
     	 proc = new_procs[i];

     	if (!proc || (max_procs && (i >= max_procs)))break;
	 
	 	user  = getpwuid(proc->uid);
     	group = getgrgid(proc->gid);

	 	if (user && user->pw_name) {
     	  	user_str = user->pw_name;
		 } else {
   		     snprintf(user_buf, 20, "%d", proc->uid);
    	    user_str = user_buf;
		 }

   		 if (group && group->gr_name) {
   	      	group_str = group->gr_name;
		 } else {
         	snprintf(group_buf, 20, "%d", proc->gid);
         	group_str = group_buf;
		 }

		 if (!threads) 
    	     if(f!=NULL)fprintf(f, "%5d %3ld %8ld%% %c %5d %6ldK %6ldK %3s %-8.8s %s\n", proc->pid, proc->delta_time,proc->delta_time*100/normal_total_delta,proc->state, proc->num_threads,
     	      	    proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->name[0] != 0 ? proc->name : proc->tname);
			 else LOGD("%5d %3ld %8ld%% %c %5d %6ldK %6ldK %3s %-8.8s %s\n", proc->pid, proc->delta_time, proc->delta_time*100/normal_total_delta,proc->state, proc->num_threads,
       	    	    proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->name[0] != 0 ? proc->name : proc->tname);
		 else
    	    if(f!=NULL) fprintf(f, "%5d %5d %3ld %8ld%% %c %6ldK %6ldK %3s %-8.8s %-15s %s\n", proc->pid, proc->tid, proc->delta_time, proc->delta_time*100/normal_total_delta,proc->state,
    	  	        proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->tname, proc->name);
			else LOGD("%5d %5d %3ld %8ld%% %c %6ldK %6ldK %3s %-8.8s %-15s %s\n", proc->pid, proc->tid, proc->delta_time, proc->delta_time*100/normal_total_delta,proc->state,
     	 	        proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->tname, proc->name);
       
  	  }

	return 0 ;
}

static int add_current_proc_anr(int proc_num, struct proc_info *proc) {
    int i;
	void* p;
	
    if (proc_num >= num_anr_procs) {
        if(	!(p = (void*)realloc(anr_procs, 2 * num_anr_procs * sizeof(struct proc_info *)))){
			if(proc) free(proc);
			return -1;
		}else{
			anr_procs = (struct proc_info **)p;
		}
		
        for (i = num_anr_procs; i < 2 * num_anr_procs; i++)
            anr_procs[i] = NULL;
        num_anr_procs = 2 * num_anr_procs;
    }
    anr_procs[proc_num] = proc;
	return 0;
}

static int get_current_procs_anr()
{
	DIR *proc_dir, *task_dir;
	struct dirent *pid_dir, *tid_dir;
	char filename[64];
	FILE *file;
	int proc_num;
	struct proc_info *proc;
	pid_t pid, tid;
	char *t = NULL;
	int i;

	proc_dir = opendir("/proc");
	if (!proc_dir) goto ERROR;

	num_anr_procs = 0;
	anr_procs = NULL;
	
	anr_procs = (struct proc_info **)calloc(INIT_PROCS * (threads ? THREAD_MULT : 1), sizeof(struct proc_info *));
	if(!anr_procs) goto ERROR;
	
	num_anr_procs = INIT_PROCS * (threads ? THREAD_MULT : 1);

//initilize the anr_procs array
	for(i=0;i<num_anr_procs;i++) anr_procs[i] = NULL;

	file = fopen("/proc/stat", "r");
    if (!file){
		LOGE("Could not open /proc/stat.get_current_procs_anr failed\n");
		anr_total_delta = 0xFFFFFFFF;
		goto ERROR;
	}else{ 
	    fscanf(file, "cpu  %lu %lu %lu %lu %lu %lu %lu", &anr_cpu.utime, &anr_cpu.ntime, &anr_cpu.stime,
    	        &anr_cpu.itime, &anr_cpu.iowtime, &anr_cpu.irqtime, &anr_cpu.sirqtime);
	    fclose(file);
	

	anr_total_delta = (anr_cpu.utime + anr_cpu.ntime + anr_cpu.stime + anr_cpu.itime
                        + anr_cpu.iowtime + anr_cpu.irqtime + anr_cpu.sirqtime)
                     - (new_cpu.utime + new_cpu.ntime + new_cpu.stime + new_cpu.itime
                        + new_cpu.iowtime + new_cpu.irqtime + new_cpu.sirqtime);
	}
	
	proc_num = 0;
	while ((pid_dir = readdir(proc_dir))) {
		if (!isdigit(pid_dir->d_name[0]))
			continue;
	
		pid = atoi(pid_dir->d_name);
			
		struct proc_info cur_proc;
			
		if (!threads) {
			proc = (struct proc_info *)malloc(sizeof(*proc));
			if(!proc){
				LOGW("Warning:alloc_proc for PID %d failed\n",pid);
				continue;
			}

			memset(proc,0,sizeof(proc_info));
			
			proc->pid = proc->tid = pid;
	
			sprintf(filename, "/proc/%d/stat", pid);
			read_stat(filename, proc); 
	
			sprintf(filename, "/proc/%d/cmdline", pid);
			read_cmdline(filename, proc);
	
			sprintf(filename, "/proc/%d/status", pid);
			read_status(filename, proc);
	
			read_policy(pid, proc);
	
			proc->num_threads = 0;
		} else {
			sprintf(filename, "/proc/%d/cmdline", pid);
			read_cmdline(filename, &cur_proc); //put process name into proc. name[PROC_NAME_LEN];
	
			sprintf(filename, "/proc/%d/status", pid);
			read_status(filename, &cur_proc); //put process uid and gid into proc.uid and proc.gid
				
			proc = NULL;
		}
	
		sprintf(filename, "/proc/%d/task", pid);
		task_dir = opendir(filename);
		if (!task_dir) {
			if(proc!=NULL)free(proc);
			continue;
		}
		while ((tid_dir = readdir(task_dir))) {
			if (!isdigit(tid_dir->d_name[0]))
				continue;
	
			if (threads) {
				tid = atoi(tid_dir->d_name);
	
				proc = (struct proc_info *)malloc(sizeof(*proc));
				if(!proc) continue;
				memset(proc,0,sizeof(proc_info));
				
				proc->pid = pid; proc->tid = tid;
	
				sprintf(filename, "/proc/%d/task/%d/stat", pid, tid);
				read_stat(filename, proc); //thread 's &proc->state, &proc->utime, &proc->stime, &proc->vss, &proc->rss
	
				read_policy(tid, proc);
	
				strcpy(proc->name, cur_proc.name);
				proc->uid = cur_proc.uid;
				proc->gid = cur_proc.gid;
	
				if(add_current_proc_anr(proc_num++, proc)==-1){ 
					goto ERROR;					
				}
			} else {
				proc->num_threads++;
			}
		}
	
		closedir(task_dir);
		task_dir = NULL;
		
		if (!threads)
			if(add_current_proc_anr(proc_num++, proc)==-1) goto ERROR;
	}

EXIT:	
	for (i = proc_num; i < num_anr_procs; i++)
		anr_procs[i] = NULL;
	closedir(proc_dir);
	proc_dir = NULL;
	//when the new_procs were created
	time_t now;
	time (&now) ;
	t = ctime(&now) ;
	if(t && anr_procs[0]!=NULL){
		for(i=0;t[i]!='\0' && i<DATA_LEN;i++)
			anr_procs[0]->date[i] = t[i];
		if(i==DATA_LEN) anr_procs[0]->date[i-1] = '\0';
		else anr_procs[0]->date[i] = '\0';

		anr_procs[0]->time = now;
	}
	
	return 0;

ERROR:
	if(proc_dir!=NULL) closedir(proc_dir);
	if(task_dir!=NULL) closedir(task_dir);
	
	if(num_anr_procs!=0 && anr_procs!=NULL){
		for(i=0;i<num_anr_procs;i++){
			if(anr_procs[i]!=NULL)
				free(anr_procs[i]);
		}
		free(anr_procs);
	}

	anr_procs = NULL;
	num_anr_procs = 0;
	
	return -1;
}


int dump_procs_anr()
{
	time_t now;
	char *t ;
	time (&now) ;
	t = ctime(&now) ;
	int i = 0;
	char temp[0x5];
	char filepath[80] = "/data/anr/anr_top" ;
	char temppath[80] ;
	FILE* f = NULL;
	int iRet = -1;
	//enter critical section
	pthread_mutex_lock(&anr_lock);
	if(!anr_top_threa_exist){
		LOGD("top monitor thread has exited, can not dump anr procs!!!!!");
		iRet = -1;
		//leave critical section
		pthread_mutex_unlock(&anr_lock);
		goto EXIT;
	}
	if(get_current_procs_anr()!=-1){
		DIR* test = opendir("/data/anr");
		int dirExist = 0;
		if(test!=NULL){
			closedir(test);
			dirExist = 1;
		}else{
			if(errno == ENOENT){
				LOGW("/data/anr not existed,will create it now\n");
				if(mkdir("/data/anr",0777)==-1) dirExist = 0;
				else dirExist = 1;
			}
		}

		if(dirExist){
			myitoa(anrFileNum, temp) ;
        	strcpy(temppath, filepath) ;
       	 	strcat(temppath, ".") ;
	        strcat(temppath, temp) ;
 	       	if((f = fopen (temppath, "w"))!=NULL)
   			{
				//time_t now;
				//char *t ;
				//time (&now) ;
				//t = ctime(&now) ;
				//fprintf (f, "\n\n") ;
				//fprintf (f, "%s",t) ;
				//fprintf (f, "\n\n") ;
    			print_anr_procs(f);
    	  	  	fclose (f) ;
				anrFileNum++ ;
	      		if (anrFileNum>=50) anrFileNum = 0 ;
			}else{
				print_anr_procs(NULL);
			}  			
		}
		else{
			print_anr_procs(NULL);
		}
		
			
		
		if(anr_procs!=NULL && num_anr_procs!=0){
			for(i=0;i<num_anr_procs;i++){
				if(anr_procs[i]!=NULL) free(anr_procs[i]);
			}			
			free(anr_procs);
		}
	}
	else{
		LOGD("get_current_procs_anr error !!!!");
		iRet = -1;
		//leave critical section
		pthread_mutex_unlock(&anr_lock);
		goto EXIT;
	}
	
	//leave critical section
	pthread_mutex_unlock(&anr_lock);

	iRet = 0;
	
EXIT:
	return iRet;
}

//entry point of top monitor thread
static void* anr_top_thread(void* useless)
{
	int argc = 3;
	char argv[3][10] = {"-t",
					  "-d",
					  "10"};
    int i, j = 0;
    int tmp;
    struct sched_param param ;
    char filepath[80] = "/data/anr/top" ;
//    sp.sched_priority = TOP1_CMD_PRIO ;

//	sched_setscheduler (0, SCHED_RR, &sp) ;
	pthread_t thread_id = pthread_self();
	LOGD("anr_top_thread tid:%d",gettid());
	param.sched_priority = 40;
	if((tmp = pthread_setschedparam(thread_id,SCHED_RR,&param))!=0){
		LOGE("failed pthread_setschedparam:%d",tmp);
		anr_top_threa_exist = 0;
		goto EXIT;	
	}	
	num_used_procs = num_free_procs = 0;
    max_procs = 0;
    delay = 10;
    iterations = -1;
    proc_cmp = &proc_cpu_cmp;
	
    for (i = 0; i < argc; i++) {
        if (!strcmp(argv[i], "-m")) {
            if (i + 1 >= argc) {
                goto PARAMCONFIG_DONE;
            }
            max_procs = atoi(argv[++i]);
            continue;
        }
        if (!strcmp(argv[i], "-n")) {
            if (i + 1 >= argc) {
                goto PARAMCONFIG_DONE;
            }
            iterations = atoi(argv[++i]);
            continue;
        }
        if (!strcmp(argv[i], "-d")) {
            if (i + 1 >= argc) {
                goto PARAMCONFIG_DONE;
            }
            delay = atoi(argv[++i]);
            continue;
        }
        if (!strcmp(argv[i], "-s")) {
            if (i + 1 >= argc) {
                goto PARAMCONFIG_DONE;
            }
            ++i;
            if (!strcmp(argv[i], "cpu")) { proc_cmp = &proc_cpu_cmp; continue; }
            if (!strcmp(argv[i], "vss")) { proc_cmp = &proc_vss_cmp; continue; }
            if (!strcmp(argv[i], "rss")) { proc_cmp = &proc_rss_cmp; continue; }
            if (!strcmp(argv[i], "thr")) { proc_cmp = &proc_thr_cmp; continue; }
            goto PARAMCONFIG_DONE;
        }
        if (!strcmp(argv[i], "-t")) { threads = 1; continue; }
        if (!strcmp(argv[i], "-h")) {
			goto PARAMCONFIG_DONE;
        }

		goto PARAMCONFIG_DONE;
    }
    if (threads && proc_cmp == &proc_thr_cmp) {
        goto PARAMCONFIG_DONE;
    }

PARAMCONFIG_DONE:
	LOGD("anr_top_thread has been created\n");
	TopFileEn = *(int*)useless;
	LOGD("TopFileEn:%d",TopFileEn);
    free_procs = NULL;
    num_new_procs = num_old_procs = 0;
    new_procs = old_procs = NULL;
	anr_procs = NULL;
	num_anr_procs = 0;

	pthread_mutex_init(&anr_lock,NULL);

//first query /proc system for cpu usage
    if(read_procs()!=-1) anr_top_threa_exist = 1;
	if(!anr_top_threa_exist) goto EXIT;
	LOGD("anr_top_thread read_procs \n");
//iterated in the following loop each 10 seconds
    while ((iterations == -1) || (iterations-- > 0)) {
    		char temp[5] ;
    		char temppath[80] ;
    		FILE *f ;
    		
        old_procs = new_procs;
        num_old_procs = num_new_procs;
        memcpy(&old_cpu, &new_cpu, sizeof(old_cpu));
		
        sleep(delay);
		//enter critical section
		pthread_mutex_lock(&anr_lock);
        if(read_procs()==-1){
			anr_top_threa_exist = 0;
			pthread_mutex_unlock(&anr_lock);
        }
        //LOGD("anr_top_thread read_procs \n");
        if(TopFileEn!=0){
			DIR* test = opendir("/data/anr");
			int dirExist = 0;
			if(test!=NULL){
				closedir(test);
				dirExist = 1;
			}else{
				if(errno == ENOENT){
					LOGW("/data/anr not existed,will create it now\n");
					if(mkdir("/data/anr",0777)==-1) dirExist = 0;
					else dirExist = 1;
				}
			}

			if(dirExist){
        		myitoa(j, temp) ;
        		strcpy(temppath, filepath) ;
        		strcat(temppath, ".") ;
        		strcat(temppath, temp) ;
        		if((f = fopen (temppath, "w"))!=NULL){
        			time_t now;
					char *t ;
					time (&now) ;
					t = ctime(&now) ;
					fprintf (f, "\n\n") ;
					fprintf (f,"%s", t) ;
					fprintf (f, "\n\n") ;
		       		print_procs(f);
		      	 	fclose (f) ;
    		  	 	// file counter ++
      			 	j++ ;
      	  			if (j>=50) j=0 ;
        		}else{
					print_procs(NULL);
				}
			}
			else{
				print_procs(NULL);
			}
        }else{
			print_procs(NULL);
		}
		
        free_old_procs();
		//leave critical section
		pthread_mutex_unlock(&anr_lock);
    }

EXIT:
	LOGE("!!!!!!!anr_top_thread exit due to the reasons!!!!!!!!\n");
    return NULL;
}

static struct proc_info *alloc_proc(void) {
    struct proc_info *proc;

    if (free_procs) {
        proc = free_procs;
        free_procs = free_procs->next;
        num_free_procs--;
    } else {
        proc = (struct proc_info *)malloc(sizeof(*proc));
        if (!proc){ 
			LOGE("Could not allocate struct process_info.\n");
			return NULL;
        }
    }

	memset(proc,0,sizeof(proc_info));
	
    num_used_procs++;

    return proc;
}

static void free_proc(struct proc_info *proc) {
    proc->next = free_procs;
    free_procs = proc;

    num_used_procs--;
    num_free_procs++;
}

#define MAX_LINE 256

static int read_procs(void) {
    DIR *proc_dir = NULL, *task_dir = NULL;
    struct dirent *pid_dir = NULL, *tid_dir = NULL;
    char filename[64];
    FILE *file = NULL;
    int proc_num = 0;
    struct proc_info *proc = NULL;
	struct proc_info* tmp1 = NULL;
	struct proc_info* tmp2 = NULL;
    pid_t pid = 0 , tid = 0;
    int i = 0;
	char *t = NULL;
	
    proc_dir = opendir("/proc");
    if (!proc_dir) {
		LOGE("Could not open /proc.top_anr_thread will exit\n");
		goto ERROR;
	}
	
    new_procs = (struct proc_info **)calloc(INIT_PROCS * (threads ? THREAD_MULT : 1), sizeof(struct proc_info *));
	if(!new_procs){
		LOGE("Could not alloc new_procs. top_anr_thread will exit\n");
		goto ERROR;
	}
	
	num_new_procs = INIT_PROCS * (threads ? THREAD_MULT : 1);

//initilize the new_procs 
	for (i = 0; i < num_new_procs; i++) new_procs[i] = NULL;

	
    file = fopen("/proc/stat", "r");
    if (!file){
		LOGE("Could not open /proc/stat.top_anr_thread will exit\n");
		goto ERROR;
	} 
	
    fscanf(file, "cpu  %lu %lu %lu %lu %lu %lu %lu", &new_cpu.utime, &new_cpu.ntime, &new_cpu.stime,
            &new_cpu.itime, &new_cpu.iowtime, &new_cpu.irqtime, &new_cpu.sirqtime);
    fclose(file);

    proc_num = 0;
    while ((pid_dir = readdir(proc_dir))) {
        if (!isdigit(pid_dir->d_name[0]))
            continue;

        pid = atoi(pid_dir->d_name);
        
        struct proc_info cur_proc;
        
        if (!threads) {
            proc = alloc_proc();
			if(!proc){
				LOGW("Warning:alloc_proc for PID %d failed\n",pid);
				continue;
			}

			proc->pid = proc->tid = pid;

            sprintf(filename, "/proc/%d/stat", pid);
            read_stat(filename, proc); 

            sprintf(filename, "/proc/%d/cmdline", pid);
            read_cmdline(filename, proc);

            sprintf(filename, "/proc/%d/status", pid);
            read_status(filename, proc);

            read_policy(pid, proc);

            proc->num_threads = 0;
        } else {
            sprintf(filename, "/proc/%d/cmdline", pid);
            read_cmdline(filename, &cur_proc); //put process name into proc. name[PROC_NAME_LEN];

            sprintf(filename, "/proc/%d/status", pid);
            read_status(filename, &cur_proc); //put process uid and gid into proc.uid and proc.gid
            
            proc = NULL;
        }

        sprintf(filename, "/proc/%d/task", pid);
        task_dir = opendir(filename);
        if (!task_dir) {
			if(proc!=NULL) free_proc(proc);
			continue;
		}

        while ((tid_dir = readdir(task_dir))) {
            if (!isdigit(tid_dir->d_name[0]))
                continue;

            if (threads) {
                tid = atoi(tid_dir->d_name);

                proc = alloc_proc();
				if(!proc){
					LOGW("Warning:alloc_proc for TID %d failed\n",tid);
					continue;
				}
                proc->pid = pid; proc->tid = tid;

                sprintf(filename, "/proc/%d/task/%d/stat", pid, tid);
                read_stat(filename, proc); //thread 's &proc->state, &proc->utime, &proc->stime, &proc->vss, &proc->rss

                read_policy(tid, proc);

                strcpy(proc->name, cur_proc.name);
                proc->uid = cur_proc.uid;
                proc->gid = cur_proc.gid;

                if(add_proc(proc_num++, proc) == -1){
					goto ERROR;
				}
            } else {
                proc->num_threads++;
            }
        }

        closedir(task_dir);
        task_dir = NULL;
		
        if (!threads){
            if(add_proc(proc_num++, proc)==-1){
				goto ERROR;
			}
        }
    }

    for (i = proc_num; i < num_new_procs; i++)
        new_procs[i] = NULL;

    closedir(proc_dir);
	proc_dir = NULL;
	//when the new_procs were created
	time_t now;
	time (&now) ;
	t = ctime(&now) ;
	if(t){
		for(i=0;t[i]!='\0' && i<DATA_LEN;i++)
			new_procs_date[i] = t[i];
		if(i==DATA_LEN) new_procs_date[i-1] = '\0';
		else new_procs_date[i] = '\0';

		new_procs_time = now;
	}

	return 0;
	
ERROR:
	if(proc_dir!=NULL) closedir(proc_dir);
	if(task_dir!=NULL) closedir(task_dir);
	if(new_procs!=NULL && num_new_procs!=0){
		for(i=0;i<num_new_procs;i++){
			if(new_procs[i]!=NULL) free(new_procs[i]);
		}
	}
	if(new_procs!=NULL) free(new_procs);

	if(old_procs!=new_procs){
		if(old_procs!=NULL && num_old_procs!=0){
			for(i=0;i<num_old_procs;i++){
				if(old_procs[i]!=NULL) free(old_procs[i]);
			}
		}
		if(old_procs!=NULL) free(old_procs);
	}

	if(free_procs!=NULL && num_free_procs!=0){
		for(tmp1 = free_procs, i=0;i<num_free_procs;i++){
			tmp2 = tmp1->next;
			free(tmp1);
			tmp1 = tmp2;
		}
	}

	new_procs = old_procs = NULL;
	free_procs = NULL;
	num_new_procs = num_old_procs = num_free_procs = 0;
	
	anr_top_threa_exist = 0;

	return -1;
}

static int read_stat(char *filename, struct proc_info *proc) {
    FILE *file;
    char buf[MAX_LINE], *open_paren, *close_paren;
    int res, idx;

    file = fopen(filename, "r");
    if (!file) return 1;
    fgets(buf, MAX_LINE, file);
    fclose(file);

    /* Split at first '(' and last ')' to get process name. */
    open_paren = strchr(buf, '(');
    close_paren = strrchr(buf, ')');
    if (!open_paren || !close_paren) return 1;

    *open_paren = *close_paren = '\0';
    strncpy(proc->tname, open_paren + 1, THREAD_NAME_LEN);
    proc->tname[THREAD_NAME_LEN-1] = 0;
    
    /* Scan rest of string. */
    sscanf(close_paren + 1, " %c %*d %*d %*d %*d %*d %*d %*d %*d %*d %*d "
                 "%lu %lu %*d %*d %*d %*d %*d %*d %*d %lu %ld",
                 &proc->state, &proc->utime, &proc->stime, &proc->vss, &proc->rss);

    return 0;
}

static int add_proc(int proc_num, struct proc_info *proc) 
{
    int i;
	void* p = NULL;
	
    if (proc_num >= num_new_procs) {
        if(!(p = realloc(new_procs, 2 * num_new_procs * sizeof(struct proc_info *)))){
			LOGE("Could not expand procs array.\n");
			if(proc!=NULL) free_proc(proc);
			return -1;
		}else{
			new_procs = (struct proc_info **)p;
		}
        for (i = num_new_procs; i < 2 * num_new_procs; i++)
            new_procs[i] = NULL;
        num_new_procs = 2 * num_new_procs;
    }
	
    new_procs[proc_num] = proc;
	return 0;
}

static int read_cmdline(char *filename, struct proc_info *proc) {
    FILE *file;
    char line[MAX_LINE];

    line[0] = '\0';
    file = fopen(filename, "r");
    if (!file) return 1;
    fgets(line, MAX_LINE, file);
    fclose(file);
    if (strlen(line) > 0) {
        strncpy(proc->name, line, PROC_NAME_LEN);
        proc->name[PROC_NAME_LEN-1] = 0;
    } else
        proc->name[0] = 0;
    return 0;
}

static void read_policy(int pid, struct proc_info *proc) {
    SchedPolicy p;
    if (get_sched_policy(pid, &p) < 0)
        strcpy(proc->policy, "unk");
    else {
        if (p == SP_BACKGROUND)
            strcpy(proc->policy, "bg");
        else if (p == SP_FOREGROUND)
            strcpy(proc->policy, "fg");
        else
            strcpy(proc->policy, "er");
    }
}

static int read_status(char *filename, struct proc_info *proc) {
    FILE *file;
    char line[MAX_LINE];
    unsigned int uid, gid;

    file = fopen(filename, "r");
    if (!file) return 1;
    while (fgets(line, MAX_LINE, file)) {
        sscanf(line, "Uid: %u", &uid);
        sscanf(line, "Gid: %u", &gid);
    }
    fclose(file);
    proc->uid = uid; proc->gid = gid;
    return 0;
}

static int print_procs(FILE *f) {
    int i;
    struct proc_info *old_proc, *proc;
    long unsigned total_delta_time;
    struct passwd *user;
    struct group *group;
    char *user_str, user_buf[20];
    char *group_str, group_buf[20];

    for (i = 0; i < num_new_procs; i++) {
        if (new_procs[i]) {
            old_proc = find_old_proc(new_procs[i]->pid, new_procs[i]->tid);
            if (old_proc) {
                new_procs[i]->delta_utime = new_procs[i]->utime - old_proc->utime;
                new_procs[i]->delta_stime = new_procs[i]->stime - old_proc->stime;
            } else {
                new_procs[i]->delta_utime = 0;
                new_procs[i]->delta_stime = 0;
            }
            new_procs[i]->delta_time = new_procs[i]->delta_utime + new_procs[i]->delta_stime;
        }
    }

    total_delta_time = (new_cpu.utime + new_cpu.ntime + new_cpu.stime + new_cpu.itime
                        + new_cpu.iowtime + new_cpu.irqtime + new_cpu.sirqtime)
                     - (old_cpu.utime + old_cpu.ntime + old_cpu.stime + old_cpu.itime
                        + old_cpu.iowtime + old_cpu.irqtime + old_cpu.sirqtime);
	normal_total_delta = total_delta_time;
    qsort(new_procs, num_new_procs, sizeof(struct proc_info *), proc_cmp);

	if(f==NULL) return 0;
	
    fprintf(f, "\n\n\n");
    fprintf(f, "User %ld%%, System %ld%%, IOW %ld%%, IRQ %ld%%\n",
            ((new_cpu.utime + new_cpu.ntime) - (old_cpu.utime + old_cpu.ntime)) * 100  / total_delta_time,
            ((new_cpu.stime ) - (old_cpu.stime)) * 100 / total_delta_time,
            ((new_cpu.iowtime) - (old_cpu.iowtime)) * 100 / total_delta_time,
            ((new_cpu.irqtime + new_cpu.sirqtime)
                    - (old_cpu.irqtime + old_cpu.sirqtime)) * 100 / total_delta_time);
    fprintf(f, "User %ld + Nice %ld + Sys %ld + Idle %ld + IOW %ld + IRQ %ld + SIRQ %ld = %ld\n",
            new_cpu.utime - old_cpu.utime,
            new_cpu.ntime - old_cpu.ntime,
            new_cpu.stime - old_cpu.stime,
            new_cpu.itime - old_cpu.itime,
            new_cpu.iowtime - old_cpu.iowtime,
            new_cpu.irqtime - old_cpu.irqtime,
            new_cpu.sirqtime - old_cpu.sirqtime,
            total_delta_time);
    fprintf(f, "\n");
    if (!threads) 
        fprintf(f, "%5s %4s %4s%% %1s %5s %7s %7s %3s %-8s %s\n", "PID", "Jeffies","CPU%", "S", "#THR", "VSS", "RSS", "PCY", "UID", "Name");
    else
        fprintf(f, "%5s %5s %4s %4s%% %1s %7s %7s %3s %-8s %-15s %s\n", "PID", "TID", "Jeffies","CPU%", "S", "VSS", "RSS", "PCY", "UID", "Thread", "Proc");

    for (i = 0; i < num_new_procs; i++) {
        proc = new_procs[i];

        if (!proc || (max_procs && (i >= max_procs)))
            break;
        user  = getpwuid(proc->uid);
        group = getgrgid(proc->gid);
        if (user && user->pw_name) {
            user_str = user->pw_name;
        } else {
            snprintf(user_buf, 20, "%d", proc->uid);
            user_str = user_buf;
        }
        if (group && group->gr_name) {
            group_str = group->gr_name;
        } else {
            snprintf(group_buf, 20, "%d", proc->gid);
            group_str = group_buf;
        }
        if (!threads) 
            fprintf(f, "%5d %3ld %8ld%% %c %5d %6ldK %6ldK %3s %-8.8s %s\n", proc->pid, proc->delta_time,proc->delta_time*100/total_delta_time,proc->state, proc->num_threads,
                proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->name[0] != 0 ? proc->name : proc->tname);
        else
            fprintf(f, "%5d %5d %3ld %8ld%% %c %6ldK %6ldK %3s %-8.8s %-15s %s\n", proc->pid, proc->tid, proc->delta_time,proc->delta_time*100/total_delta_time, proc->state,
                proc->vss / 1024, proc->rss * getpagesize() / 1024, proc->policy, user_str, proc->tname, proc->name);
       
    }
    return 0 ;
}

static struct proc_info *find_old_proc(pid_t pid, pid_t tid) {
    int i;

    for (i = 0; i < num_old_procs; i++)
        if (old_procs[i] && (old_procs[i]->pid == pid) && (old_procs[i]->tid == tid))
            return old_procs[i];

    return NULL;
}

static void free_old_procs(void) {
    int i;

    for (i = 0; i < num_old_procs; i++)
        if (old_procs[i])
            free_proc(old_procs[i]);

    free(old_procs);
}

static int proc_cpu_cmp(const void *a, const void *b) {
    struct proc_info *pa, *pb;

    pa = *((struct proc_info **)a); pb = *((struct proc_info **)b);

    if (!pa && !pb) return 0;
    if (!pa) return 1;
    if (!pb) return -1;

    return -numcmp(pa->delta_time, pb->delta_time);
}

static int proc_vss_cmp(const void *a, const void *b) {
    struct proc_info *pa, *pb;

    pa = *((struct proc_info **)a); pb = *((struct proc_info **)b);

    if (!pa && !pb) return 0;
    if (!pa) return 1;
    if (!pb) return -1;

    return -numcmp(pa->vss, pb->vss);
}

static int proc_rss_cmp(const void *a, const void *b) {
    struct proc_info *pa, *pb;

    pa = *((struct proc_info **)a); pb = *((struct proc_info **)b);

    if (!pa && !pb) return 0;
    if (!pa) return 1;
    if (!pb) return -1;

    return -numcmp(pa->rss, pb->rss);
}

static int proc_thr_cmp(const void *a, const void *b) {
    struct proc_info *pa, *pb;

    pa = *((struct proc_info **)a); pb = *((struct proc_info **)b);

    if (!pa && !pb) return 0;
    if (!pa) return 1;
    if (!pb) return -1;

    return -numcmp(pa->num_threads, pb->num_threads);
}

static int numcmp(long long a, long long b) {
    if (a < b) return -1;
    if (a > b) return 1;
    return 0;
}

static void usage(char *cmd) {
    fprintf(stderr, "Usage: %s [ -m max_procs ] [ -n iterations ] [ -d delay ] [ -s sort_column ] [ -t ] [ -h ]\n"
                    "    -m num  Maximum number of processes to display.\n"
                    "    -n num  Updates to show before exiting.\n"
                    "    -d num  Seconds to wait between updates.\n"
                    "    -s col  Column to sort by (cpu,vss,rss,thr).\n"
                    "    -t      Show threads instead of processes.\n"
                    "    -h      Display this help screen.\n",
        cmd);
}

char*
myitoa(int i, char* buf)
{
    char* result = buf;
		char* p ;
		
    // Handle negative
    if (i < 0)
    {
        *buf++ = '-';
        i = -i;
    }

    // Output digits in reverse order
    p = buf;
    do
    {
        *p++ = (char)('0' + (i % 10));
        i /= 10;
    }
    while (i);
    *p-- = 0;

    // Reverse the string
    while (buf < p)
    {
        char c = *buf;
        *buf++ = *p;
        *p-- = c;
    }

    return result;
}

