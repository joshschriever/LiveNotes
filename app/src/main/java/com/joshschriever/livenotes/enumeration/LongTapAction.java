package com.joshschriever.livenotes.enumeration;

import com.joshschriever.livenotes.R;

import java8.util.function.Consumer;

public enum LongTapAction {
    START(R.string.start_description, ActionVisitor::startRecording),
    STOP(R.string.stop_description, ActionVisitor::stopRecording),
    SAVE(R.string.save_description, ActionVisitor::saveScore),
    RESET(R.string.reset_description, ActionVisitor::resetScore);

    private int description;
    private Consumer<ActionVisitor> action;

    LongTapAction(int description, Consumer<ActionVisitor> action) {
        this.description = description;
        this.action = action;
    }

    public void showDescription(ActionVisitor actionVisitor) {
        actionVisitor.showDescription(description);
    }

    public void takeAction(ActionVisitor actionVisitor) {
        action.accept(actionVisitor);
    }

    public interface ActionVisitor {

        void startRecording();

        void stopRecording();

        void saveScore();

        void resetScore();

        void showDescription(int descriptionResId);
    }

}
