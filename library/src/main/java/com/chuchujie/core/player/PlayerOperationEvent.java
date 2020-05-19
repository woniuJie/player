package com.chuchujie.core.player;

/**
 * Created by yxb on 2018/3/15.
 */

public class PlayerOperationEvent {

    //1：长按，2点击
    public static final int PLAYER_OP_EVENT_1 = 1;
    //1：download video file
    int operation;
    public String url;

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public PlayerOperationEvent(int operation){
        this.operation = operation;
    }

}
