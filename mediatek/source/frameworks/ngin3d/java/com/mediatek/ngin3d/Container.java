package com.mediatek.ngin3d;

import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.presentation.Presentation;
import com.mediatek.ngin3d.presentation.PresentationEngine;

import java.util.List;

/**
 * Container is a special actor that contains other actors. It provides API for adding, removing,
 * and iterating on contained actors.
 */
public class Container<T extends Presentation> extends Group<T> {

    private int mScreenWidth;
    private int mScreenHeight;
    private Transition mTransition;
    private boolean mTransitionComplete = true;

    /**
     * @hide
     */
    @Override
    protected T createPresentation(PresentationEngine engine) {
        mScreenWidth = engine.getWidth();
        mScreenHeight = engine.getHeight();
        return super.createPresentation(engine);
    }

    /**
     * Add the actors into this container.
     *
     * @param actors actors to add as children
     */
    public void add(Actor... actors) {
        addChild(actors);
    }

    Transition.TransitionListener mTransitionListener = new Transition.TransitionListener() {
        public void onActorAppear(Actor actor) {
            add(actor);
        }

        public void onActorDisappear(Actor actor) {
            remove(actor);
        }
    };

    @Override
    protected void onChildAdded(Actor child) {
        if (mTransition != null && !mTransition.isAnimationInProgress(child)) {
            mTransition.startTransition(child, Transition.ANI_TYPE_IN, mTransitionListener);
        }
    }

    @Override
    protected void onChildRemoved(Actor child) {
        // Do nothing
    }

    /**
     * Remove the actor from this container.
     *
     * @param child
     */
    public void remove(Actor child) {
        if (mTransition == null || mTransition.isAnimationInProgress(child)) {
            removeChild(child);
        } else {
            mTransition.startTransition(child, Transition.ANI_TYPE_OUT, mTransitionListener);
        }
    }

    /**
     * Replace Actor 1 with Actor 2.
     *
     * @param from Actor 1
     * @param to Actor 2
     */
    public void replace(Actor from, Actor to) {
        if (mTransition != null) {

            if (!mChildren.contains(from) && !mTransition.isAnimationInProgress(from)) {
                throw new NullPointerException("The actor to be replaced is not in this container.");
            }

            if (mChildren.contains(to) && !mTransition.isAnimationInProgress(to)) {
                throw new IllegalArgumentException("This actor is already in this container.");
            }

            Animation.Listener aniListener = new Animation.Listener() {
                public void onStarted(Animation animation) {
                    mTransitionComplete = false;
                }

                public void onCompleted(Animation animation) {
                    mTransitionComplete = true;
                }
            };
            mTransition.addListener(aniListener);
            mTransition.startTransition(from, to, mTransitionListener);
        }
    }

    public boolean isTransitionComplete() {
        return mTransitionComplete;
    }

    public void removeAll() {
        removeAllChildren();
    }

    /**
     * Gets the number of children in Container.
     * @return  the number of actors
     */
    public int getChildrenCount() {
        return super.getChildrenCount();
    }

    /**
     * Gets the number of descendant in Container.
     * @return  the number of actors
     */
    public int getDescendantCount() {
        return super.getDescendantCount();
    }

    /**
     * Gets the child in Container with index
     * @param index  the index of child
     * @return the  child with specific index
     */
    public <T> T getChild(int index) {
        return super.<T>getChildByIndex(index);
    }

    /**
     * Gets the all children in Container.
     * @return  the List of children
     */
    public List<Actor> getChildren() {
        return super.getAllChildren();
    }

    /**
     * Gets the specific actor with the name from children of Container.
     * @param  childName  name of actor.
     * @return  actor object, or null if the specific actor is not existed in this Container.
     */
    public Actor findChildByName(CharSequence childName) {
        return super.findChildByName(childName);
    }

    /**
     * Gets the specific actor with the tag from children of Container.
     * @param tag  tag of actor.
     * @return  actor object, or null if the specific actor is not existed in this Container.
     */
    public Actor findChildByTag(int tag) {
        return super.findChildByTag(tag);
    }

    /**
     * Gets the specific actor with the id from descendant of Container.
     * @param childId  id of actor.
     * @return  actor object, or null if the specific actor is not existed in this Container.
     */
    public Actor findChildById(int childId) {
        return super.findChildById(childId);
    }

    /**
     * Gets the specific actor with the name from descendant of Container.
     * @param childName  name of actor.
     * @param searchMode  0 is depth first search and 1 is breadth first search, otherwise search first level only.
     * @return  actor object, or null if the specific actor is not existed in this Container.
     */
    public Actor findChildByName(CharSequence childName, int searchMode) {
        return super.findChildByName(childName, searchMode);
    }

    /**
     * Gets the specific actor with the tag from descendant of Container.
     * @param tag  tag of actor.
     * @return  actor object, or null if the specific actor is not existed in this Container.
     */
    public Actor findChildByTag(int tag, int searchMode) {
        return super.findChildByTag(tag, searchMode);
    }

    /**
     * Gets the specific actor with the id from descendant of Container.
     * @param childId  id of actor.
     * @param searchMode  0 is depth first search and 1 is breadth first search, otherwise search first level only.
     * @return  actor object, or null if the specific actor is not existed in this Container.
     */
    public Actor findChildById(int childId, int searchMode) {
        return super.findChildById(childId, searchMode);
    }

    public void raise(Actor child, Actor sibling) {
        raiseChild(child, sibling);
    }

    public void lower(Actor child, Actor sibling) {
        lowerChild(child, sibling);
    }

    /**
     * Sets up transition effects with specific arguments.
     *
     * @param goInEffects  arguments for go in animation.
     * @param goOutEffects arguments for go our animation
     */
    public void setTransition(int goInEffects, int goOutEffects) {
        mTransition = new Transition(mScreenWidth, mScreenHeight, goInEffects, goOutEffects);
    }

    /**
     * Sets up transition effects with specific argument.
     *
     * @param effects arguments for both go in and go out animation.
     */
    public void setTransition(int effects) {
        mTransition = new Transition(mScreenWidth, mScreenHeight, effects);
    }

    /**
     * Sets up transition effects with user customize animation.
     *
     * @param goIn  animation group for go in effect.
     * @param goOut animation group for go out effect.
     */
    public void setTransition(AnimationGroup goIn, AnimationGroup goOut) {
        mTransition = new Transition(goIn, goOut);
    }

    public Transition getTransition() {
        return mTransition;
    }
}
