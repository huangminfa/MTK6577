package com.mediatek.ngin3d;

import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.presentation.PresentationEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * A mechanism for batching multiple model-tree operations into atomic updates to the render tree. Besides, it will also help
 * to build animations for property change.
 */
public abstract class Transaction {
    /**
     * @hide
     */
    protected int mAnimationDuration = BasicAnimation.DEFAULT_DURATION;
    /**
     * @hide
     */
    protected Mode mAlphaMode = Mode.EASE_IN_OUT_QUAD;
    /**
     * @hide
     */
    protected Runnable mCompletion;

    /**
     * @hide
     */
    protected abstract class Modification {
        /**
         * Override to apply the modifications.
         */
        protected abstract void apply();
    }

    private static PresentationEngine.RenderCallback sRenderCallback;

    ///////////////////////////////////////////////////////////////////////////
    // static Transaction states

    private static ThreadLocal sTransactionStack = new ThreadLocal();
    private static List<Modification> sCommittedOperations = Collections.synchronizedList(new ArrayList<Modification>());
    private static List<Modification> sOperationsToApply = Collections.synchronizedList(new ArrayList<Modification>());

    private static Stack<Transaction> getTransactionStack() {
        Stack<Transaction> stack = (Stack<Transaction>) sTransactionStack.get();
        if (stack == null) {
            stack = new Stack<Transaction>();
            sTransactionStack.set(stack);
        }
        return stack;
    }

    /**
     * @hide
     */
    protected static List<Modification> getModificationList() {
        return sCommittedOperations;

    }

    /**
     * Begin a new transaction.
     */
    private static void begin(Transaction transaction) {
        Stack<Transaction> stack = getTransactionStack();
        stack.push(transaction);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Transaction API

    /**
     * @return active transaction or null if no transaction is active.
     */
    public static Transaction getActive() {
        Stack<Transaction> stack = getTransactionStack();
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    public static ImplicitAnimation beginImplicitAnimation() {
        ImplicitAnimation animation = new ImplicitAnimation();
        begin(animation);
        return animation;
    }

    public static BatchPropertyModification beginPropertiesModification() {
        BatchPropertyModification modification = new BatchPropertyModification();
        begin(modification);
        return modification;
    }

    /**
     * Change animation duration in active transaction.
     *
     * @param duration in milliseconds
     */
    public static void setAnimationDuration(int duration) {
        Transaction transaction = getActive();
        if (transaction != null) {
            transaction.mAnimationDuration = duration;
        }
    }

    /**
     * Change the alpha mode in active transaction.
     *
     * @param mode alpha mode, such as Mode.LINEAR.
     */
    public static void setAlphaMode(Mode mode) {
        Transaction transaction = getActive();
        if (transaction != null) {
            transaction.mAlphaMode = mode;
        }
    }

    public static void setCompletion(Runnable completion) {
        Transaction transaction = getActive();
        if (transaction != null) {
            transaction.mCompletion = completion;
        }
    }

    /**
     * Commit current transaction.
     */
    public static void commit() {
        Stack<Transaction> stack = getTransactionStack();
        stack.pop();
        if (stack.isEmpty()) {
            sOperationsToApply.addAll(sCommittedOperations);
            sCommittedOperations.clear();
            if (sRenderCallback != null) {
                sRenderCallback.requestRender();
            }
        }
    }

    /**
     * Commit all transactions.
     */
    public static void commitAll() {
        Stack<Transaction> stack = getTransactionStack();
        stack.clear();
        sOperationsToApply.addAll(sCommittedOperations);
        sCommittedOperations.clear();

        if (sRenderCallback != null) {
            sRenderCallback.requestRender();
        }
    }

    public static void applyOperations() {
        Stack<Transaction> stack = getTransactionStack();
        if (stack.isEmpty()) {
            synchronized (sOperationsToApply) {
                for (Modification modification : sOperationsToApply) {
                    modification.apply();
                }
                sOperationsToApply.clear();
            }
        }
    }

    public static void setRenderCallback(PresentationEngine.RenderCallback renderCallback) {
        sRenderCallback = renderCallback;
    }

    /**
     * Override to add property modification.
     *
     * @param target   target actor
     * @param property property to change
     * @param value    the new value
     */
    public abstract void addPropertyModification(Actor target, Property property, Object value);
}

