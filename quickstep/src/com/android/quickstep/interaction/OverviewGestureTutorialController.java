/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.quickstep.interaction;

import static com.android.launcher3.anim.Interpolators.ACCEL;
import static com.android.quickstep.interaction.TutorialController.TutorialType.OVERVIEW_NAVIGATION_COMPLETE;

import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.graphics.PointF;
import android.os.Build;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.launcher3.R;
import com.android.launcher3.anim.PendingAnimation;
import com.android.quickstep.AnimatedFloat;
import com.android.quickstep.SwipeUpAnimationLogic;
import com.android.quickstep.interaction.EdgeBackGestureHandler.BackGestureResult;
import com.android.quickstep.interaction.NavBarGestureHandler.NavBarGestureResult;

/** A {@link TutorialController} for the Overview tutorial. */
@TargetApi(Build.VERSION_CODES.R)
final class OverviewGestureTutorialController extends SwipeUpGestureTutorialController {

    OverviewGestureTutorialController(OverviewGestureTutorialFragment fragment,
            TutorialType tutorialType) {
        super(fragment, tutorialType);
    }

    @Override
    Integer getTitleStringId() {
        switch (mTutorialType) {
            case OVERVIEW_NAVIGATION:
                return R.string.overview_gesture_intro_title;
            case OVERVIEW_NAVIGATION_COMPLETE:
                return R.string.gesture_tutorial_confirm_title;
        }
        return null;
    }

    @Override
    Integer getSubtitleStringId() {
        if (mTutorialType == TutorialType.OVERVIEW_NAVIGATION) {
            return R.string.overview_gesture_intro_subtitle;
        }
        return null;
    }

    @Override
    Integer getActionButtonStringId() {
        if (mTutorialType == OVERVIEW_NAVIGATION_COMPLETE) {
            return R.string.gesture_tutorial_action_button_label_done;
        }
        return null;
    }

    @Nullable
    @Override
    public View getMockLauncherView() {
        return null;
    }

    @Override
    public void onBackGestureAttempted(BackGestureResult result) {
        switch (mTutorialType) {
            case OVERVIEW_NAVIGATION:
                switch (result) {
                    case BACK_COMPLETED_FROM_LEFT:
                    case BACK_COMPLETED_FROM_RIGHT:
                    case BACK_CANCELLED_FROM_LEFT:
                    case BACK_CANCELLED_FROM_RIGHT:
                        showFeedback(R.string.overview_gesture_feedback_swipe_too_far_from_edge);
                        break;
                }
                break;
            case OVERVIEW_NAVIGATION_COMPLETE:
                if (result == BackGestureResult.BACK_COMPLETED_FROM_LEFT
                        || result == BackGestureResult.BACK_COMPLETED_FROM_RIGHT) {
                    mTutorialFragment.closeTutorial();
                }
                break;
        }
    }

    @Override
    public void onNavBarGestureAttempted(NavBarGestureResult result, PointF finalVelocity) {
        if (mHideFeedbackEndAction != null) {
            return;
        }
        switch (mTutorialType) {
            case OVERVIEW_NAVIGATION:
                switch (result) {
                    case HOME_GESTURE_COMPLETED: {
                        animateFakeTaskViewHome(finalVelocity, () -> {
                            resetFakeTaskView();
                            showFeedback(R.string.overview_gesture_feedback_home_detected);
                        });
                        break;
                    }
                    case HOME_NOT_STARTED_TOO_FAR_FROM_EDGE:
                    case OVERVIEW_NOT_STARTED_TOO_FAR_FROM_EDGE:
                        showFeedback(R.string.overview_gesture_feedback_swipe_too_far_from_edge);
                        break;
                    case OVERVIEW_GESTURE_COMPLETED:
                        PendingAnimation anim = new PendingAnimation(300);
                        anim.setFloat(mTaskViewSwipeUpAnimation
                                .getCurrentShift(), AnimatedFloat.VALUE, 1, ACCEL);
                        AnimatorSet animset = anim.buildAnim();
                        animset.start();
                        mRunningWindowAnim = SwipeUpAnimationLogic.RunningWindowAnim.wrap(animset);
                        onMotionPaused(true /*arbitrary value*/);
                        showFeedback(R.string.overview_gesture_feedback_complete,
                                mTutorialFragment::continueTutorial);
                        break;
                    case HOME_OR_OVERVIEW_NOT_STARTED_WRONG_SWIPE_DIRECTION:
                    case HOME_OR_OVERVIEW_CANCELLED:
                        fadeOutFakeTaskView(false, true, null);
                        showFeedback(R.string.overview_gesture_feedback_wrong_swipe_direction);
                        break;
                }
                break;
            case OVERVIEW_NAVIGATION_COMPLETE:
                if (result == NavBarGestureResult.HOME_GESTURE_COMPLETED) {
                    mTutorialFragment.closeTutorial();
                }
                break;
        }
    }
}
