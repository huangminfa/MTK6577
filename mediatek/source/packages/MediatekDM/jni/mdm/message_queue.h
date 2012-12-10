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

/**
 * @file message_queue.h
 *
 * @brief Lightweight message queue implementation.
 *
 * There are three kinds of message queues provided: Circular Queue, Internal
 * Queue and External Queue. Circular Queue is a plain circular queue
 * implementation. Internal Queue extends Circular Queue with the support for
 * auto copy mechanism which can be enabled/disabled when the queue is created.
 * It is intended to be used in single thread. External Queue extends Internal
 * Queue with the support for concurrency protection and blocking send/receive
 * semanteme. External Queue is intended to be used inter-thread.
 *
 * Auto Copy: If the message is a reference message (size is greater than 0 and
 *            the payload is pointed by val_ptr) and auto copy is enabled, the 
 *            payload is copied when the message is enqueued. It's a convenient
 *            mechanism for passing messages around. If some of the messages are
 *            very large, you can disable this feature and apply your own
 *            message lifecycle policy.
 */

#ifndef __MESSAGE_QUEUE__
#define __MESSAGE_QUEUE__

#include "common.h"
#include "module.h"
#include "message_definition.h"

#ifdef __cplusplus
extern "C" {
#endif


/**
 * Result for message queue operations.
 */
typedef enum mdm_message_queue_result_e {
	/** Success */
	MQ_RESULT_OK = 0,
	/** Generic error */
	MQ_RESULT_GENERIC_ERROR = -1,
	/** Memory error, ex. malloc failed */
	MQ_RESULT_MEMORY_ERROR = -2,
	/** Queue is full */
	MQ_RESULT_QUEUE_IS_FULL = -3,
	/** Queue is empty */
	MQ_RESULT_QUEUE_IS_EMPTY = -4,
} mdm_message_queue_result;
	
/** Identifier of a message. */
typedef int mdm_message_id;

/** Identifer of a message queue. */
typedef int mdm_message_queue_id;

/**
 * Header of message. It will be copied to message queue when enqueued and will
 * be copied out when dequeued. Whether to copy the payload is depending on the 
 * type of message (value message or reference message) and the auto copy 
 * policy.
 */
typedef struct mdm_message_s {
	/** Source module. */
	mdm_module_id src;
	/** Destination module. */
	mdm_module_id dst;
	/** Message ID. */
	mdm_message_id id;
	/**
	 * Size of @ref val_ptr in bytes. If this is a value message, 
	 * then size should be 0, or size should be the actual size of
	 * data pointed by val_ptr and should be greater than 0. You should
	 * comply with this rule or the auto copy mechanism will break. Use
	 * MDM_VAL_MSG and MDM_REF_MSG to initialize your message is
	 * recommanded.
	 */
	size_t size;
	/** Payload of message. */
	union {
	    /** Pointer to the referenced payload. */
		void *val_ptr;
	    /** Stores the value payload. */
		int val;
	} data;
} mdm_message;

/**
 * Initialize a value message. The payload is cast to an int and stored
 * in val.
 */
#define MDM_VAL_MSG(_src, _dst, _id, _val) { \
	.src = _src,							 \
	.dst = _dst,							 \
	.id = _id,								 \
	.size = 0,								 \
	.data = {								 \
		.val = _val,						 \
	},										 \
}

/**
 * Initialize a reference message. The payload is stored in val_ptr. If
 * auto copy is enable on the queue, then the payload is copied when 
 * this message is enqueued. 
 * NOTE: size must be greated than 0 or the auto copy mechanism will break.
 */
#define MDM_REF_MSG(_src, _dst, _id, _size, _ref) { \
	.src = _src,									\
	.dst = _dst,									\
	.id = _id,										\
	.size = _size,									\
	.data = {										\
		.val_ptr = _ref,							\
	},												\
}

#ifdef DEBUG
/**
 * Dump the content of a message.
 *
 * @param	msg		The message to be dumped.
 */
extern void mdm_mq_message_dump(const mdm_message *msg);
#endif /* DEBUG */


/**
 * Circular queue. Simple circular queue without priority or synchronization.
 * You can create a queue for each priority level to implement your priority 
 * policy.
 *
 * Please do <b>NOT</b> access the fields directly.
 */
typedef struct mdm_circular_queue_s {
	/** user defined ID */
	mdm_message_queue_id id;
	/** capacity of the queue */
	size_t capacity;
	/** index of queue head */
	size_t start;
	/** length of current queue */
	size_t length;
	/** pointer to queue data */
	mdm_message *queue;
} mdm_circular_queue;

/**
 * Initialize circular queue.
 *
 * @param mq        Pointer to message queue.
 * @param id        ID of circular queue. This value is a tag for user and is 
 *                  not used internally.
 * @param capacity  Capacity of queue. This function will allocate resources for
 *                  this queue according
 *                  to this value.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_init(mdm_circular_queue *mq, mdm_message_queue_id id, size_t capacity);

/**
 * De-initialize circular queue. It will release all the payloads of reference
 * messages, too. Always return MQ_RESULT_OK.
 *
 * @param mq        Pointer to message queue.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_fini(mdm_circular_queue *mq);

/**
 * Get user defined ID.
 *
 * @param mq        Pointer to message queue.
 * @return          The message queue id of this queue.
 */
extern mdm_message_queue_id mdm_cq_get_id(const mdm_circular_queue *mq);

/**
 * Add a message to the head of queue. Message header is copied into queue, but
 * the payload is not.
 *
 * @param mq        Pointer to message queue.
 * @param in_msg    Message to add.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_add_to_head(mdm_circular_queue *mq, const mdm_message *in_msg);

/**
 * Add a message to the tail of queue. Message header is copied into queue, but
 * the payload is not.
 *
 * @param mq        Pointer to message queue.
 * @param in_msg    Message to add.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_add_to_tail(mdm_circular_queue *mq, const mdm_message *in_msg);

/**
 * Get a message from the head of queue. Message header is dequeueed from the queue.
 *
 * @param mq        Pointer to message queue.
 * @param out_msg   Out buffer for the message dequeued.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_get_head(mdm_circular_queue *mq, mdm_message *out_msg);

/**
 * Get a message from the tail of queue. Message header is dequeueed from the queue.
 *
 * @param mq        Pointer to message queue.
 * @param out_msg   Out buffer for the message dequeued.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_get_tail(mdm_circular_queue *mq, mdm_message *out_msg);

/**
 * Peek a message at the head of queue. Message header is untouched in the queue
 * and all the changes on this message will be applied to the message in queue.
 *
 * @param mq        Pointer to message queue.
 * @param out_msg   Out buffer for a pointer to the message peeked.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_peek_head(const mdm_circular_queue *mq, mdm_message **out_msg);

/**
 * Peek a message at the tail of queue. Message header is untouched in the queue
 * and all the changes on this message will be applied to the message in queue.
 *
 * @param mq        Pointer to message queue.
 * @param out_msg   Out buffer for a pointer to the message peeked.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_peek_tail(const mdm_circular_queue *mq, mdm_message **out_msg);

/**
 * A convenient wrapper for mdm_cq_add_to_tail.
 *
 * @param mq        Pointer to message queue.
 * @param in_msg    Message to enqueue.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_enqueue(mdm_circular_queue *mq, const mdm_message *in_msg);

/**
 * A convenient wrapper for mdm_cq_get_head.
 *
 * @param mq        Pointer to message queue.
 * @param out_msg   Out buffer for the message dequeued.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_dequeue(mdm_circular_queue *mq, mdm_message *out_msg);

/**
 * A convenient wrapper for mdm_cq_peek_head.
 *
 * @param mq        Pointer to message queue.
 * @param out_msg   Out buffer for a pointer to the message peeked.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_cq_peek(const mdm_circular_queue *mq, mdm_message **out_msg);

/**
 * Get the number of messages in the queue.
 *
 * @param mq        Pointer to message queue.
 * @return          Length of the queue.
 */
extern int mdm_cq_get_queue_length(const mdm_circular_queue *mq);

/**
 * Get a capacity of the queue.
 *
 * @param mq        Pointer to message queue.
 * @return          The capacity of the queue.
 */
extern int mdm_cq_get_queue_capacity(const mdm_circular_queue *mq);

/**
 * Whether the queue is empty or not.
 *
 * @param mq        Pointer to message queue.
 * @return          MDM_TRUE if empty, MDM_FALSE otherwise.
 */
extern int mdm_cq_is_empty(const mdm_circular_queue *mq);

/**
 * Whether the queue is full or not.
 *
 * @param mq        Pointer to message queue.
 * @return          MDM_TRUE if full, MDM_FALSE otherwise.
 */
extern int mdm_cq_is_full(const mdm_circular_queue *mq);

#ifdef DEBUG
/**
 * Dump the content of a message queue.
 *
 * @param msg       The message queue to be dumped.
 */
extern void mdm_cq_queue_dump(const mdm_circular_queue *mq);
#endif /* DEBUG */

/**
 * Internal queue. Intra-thread messaging without lock protection.
 *
 * NOTE: Pleas keep queue as the first field.
 */
typedef struct mdm_internal_queue_s {
    /** mdm_internal_queue is built upon mdm_circular_queue */
    mdm_circular_queue queue;
    /** automatically copy message payload or not */
    int auto_copy;
} mdm_internal_queue;

/**
 * Initialize internal queue.
 *
 * @param mq        Pointer to message queue.
 * @param id        ID of internal queue. This value is a tag for user and is not used internally.
 * @param capacity  Capacity of queue. This function will allocate resources for this queue according
 *                  to this value.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_iq_init(mdm_internal_queue *mq, mdm_message_queue_id id, size_t length, int auto_copy);

/**
 * De-initialize internal queue.  It will release all the payloads of reference
 * messages, too. Always return MQ_RESULT_OK.
 *
 * @param mq        Pointer to message queue.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_iq_fini(mdm_internal_queue *mq);

/** @copydoc mdm_cq_get_id */
extern mdm_message_queue_id mdm_iq_get_id(const mdm_internal_queue *mq);
/** @copydoc mdm_cq_add_to_head */
extern mdm_message_queue_result mdm_iq_add_to_head(mdm_internal_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_cq_add_to_tail */
extern mdm_message_queue_result mdm_iq_add_to_tail(mdm_internal_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_cq_get_head */
extern mdm_message_queue_result mdm_iq_get_head(mdm_internal_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_cq_get_tail */
extern mdm_message_queue_result mdm_iq_get_tail(mdm_internal_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_cq_peek_head */
extern mdm_message_queue_result mdm_iq_peek_head(const mdm_internal_queue *mq, mdm_message **out_msg);
/** @copydoc mdm_cq_peek_tail */
extern mdm_message_queue_result mdm_iq_peek_tail(const mdm_internal_queue *mq, mdm_message **out_msg);
/** @copydoc mdm_cq_enqueue */
extern mdm_message_queue_result mdm_iq_enqueue(mdm_internal_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_cq_dequeue */
extern mdm_message_queue_result mdm_iq_dequeue(mdm_internal_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_cq_send */
extern mdm_message_queue_result mdm_iq_send(mdm_internal_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_cq_receive */
extern mdm_message_queue_result mdm_iq_receive(mdm_internal_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_cq_peek */
extern mdm_message_queue_result mdm_iq_peek(const mdm_internal_queue *mq, mdm_message **out_msg);
/** @copydoc mdm_cq_get_queue_length */
extern int mdm_iq_get_queue_length(const mdm_internal_queue *mq);
/** @copydoc mdm_cq_get_queue_capacity */
extern int mdm_iq_get_queue_capacity(const mdm_internal_queue *mq);
/** @copydoc mdm_cq_is_empty */
extern int mdm_iq_is_empty(const mdm_internal_queue *mq);
/** @copydoc mdm_cq_is_full */
extern int mdm_iq_is_full(const mdm_internal_queue *mq);
#ifdef DEBUG
/** @copydoc mdm_cq_get_id */
extern void mdm_iq_queue_dump(const mdm_internal_queue *mq);
#endif /* DEBUG */


/**
 * External queue. Inter-thread messaging with lock protection.
 * Enqueue will block if queue is full, and dequeue will block if
 * queue is empty.
 */
typedef struct mdm_external_queue_s {
    /** mdm_external_queue is built upon mdm_internal_queue */
    mdm_internal_queue queue;
    /** mutex */
    pthread_mutex_t mutex;
    /** conditional variable for blocking enqueue */
    pthread_cond_t enqueue_cond;
    /** conditional variable for blocking dequeue */
    pthread_cond_t dequeue_cond;
} mdm_external_queue;

/**
 * Initialize external queue.
 *
 * <b>Note:</b> This function is <b>NOT</b> thread-safe.
 *
 * @param mq        Pointer to message queue.
 * @param id        ID of internal queue. This value is a tag for user and is not used internally.
 * @param capacity  Capacity of queue. This function will allocate resources for this queue according
 *                  to this value.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_eq_init(mdm_external_queue *mq, mdm_message_queue_id id, size_t length, int auto_copy);

/**
 * De-initialize external queue. It will release all the payloads of reference
 * messages, too. Always return MQ_RESULT_OK.
 *
 * <b>Note:</b> This fuction is <b>NOT</b> thread-safe.
 *
 * @param mq        Pointer to message queue.
 * @return          MQ_RESULT_OK if succeeded, others means error.
 */
extern mdm_message_queue_result mdm_eq_fini(mdm_external_queue *mq);

/** @copydoc mdm_iq_get_id */
extern mdm_message_queue_id mdm_eq_get_id(const mdm_external_queue *mq);
/** @copydoc mdm_iq_add_to_head */
extern mdm_message_queue_result mdm_eq_add_to_head(mdm_external_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_iq_add_to_tail */
extern mdm_message_queue_result mdm_eq_add_to_tail(mdm_external_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_iq_get_head */
extern mdm_message_queue_result mdm_eq_get_head(mdm_external_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_iq_get_tail */
extern mdm_message_queue_result mdm_eq_get_tail(mdm_external_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_iq_peek_head */
extern mdm_message_queue_result mdm_eq_peek_head(mdm_external_queue *mq, mdm_message **out_msg);
/** @copydoc mdm_iq_peek_tail */
extern mdm_message_queue_result mdm_eq_peek_tail(mdm_external_queue *mq, mdm_message **out_msg);
/** @copydoc mdm_iq_enqueue */
extern mdm_message_queue_result mdm_eq_enqueue(mdm_external_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_iq_dequeue */
extern mdm_message_queue_result mdm_eq_dequeue(mdm_external_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_iq_send */
extern mdm_message_queue_result mdm_eq_send(mdm_external_queue *mq, const mdm_message *in_msg);
/** @copydoc mdm_iq_receive */
extern mdm_message_queue_result mdm_eq_receive(mdm_external_queue *mq, mdm_message *out_msg);
/** @copydoc mdm_iq_peek */
extern mdm_message_queue_result mdm_eq_peek(mdm_external_queue *mq, mdm_message **out_msg);
/** @copydoc mdm_iq_get_queue_length */
extern int mdm_eq_get_queue_length(mdm_external_queue *mq);
/** @copydoc mdm_iq_get_queue_capacity */
extern int mdm_eq_get_queue_capacity(mdm_external_queue *mq);
/** @copydoc mdm_iq_is_empty */
extern int mdm_eq_is_empty(mdm_external_queue *mq);
/** @copydoc mdm_iq_is_full */
extern int mdm_eq_is_full(mdm_external_queue *mq);
#ifdef DEBUG
/** @copydoc mdm_iq_queue_dump */
extern void mdm_eq_queue_dump(const mdm_external_queue *mq);
#endif /* DEBUG */


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* __MESSAGE_QUEUE__ */
