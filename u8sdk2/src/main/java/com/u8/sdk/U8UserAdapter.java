package com.u8.sdk;


public abstract class U8UserAdapter implements IUser {

    @Override
    public abstract boolean isSupportMethod(String methodName);


    @Override
    public void login() {
    }

    @Override
    public void loginCustom(String customData) {
    }

    @Override
    public void switchLogin() {
    }

    @Override
    public boolean showAccountCenter() {
        return false;
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {

    }

    @Override
    public void logout() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void postGiftCode(String code) {

    }


    @Override
    public void realNameRegister() {

    }

    @Override
    public void queryAntiAddiction() {

    }


    @Override
    public void showCallCenter() {

    }

    @Override
    public void showPrivacyPolicy() {

    }

    @Override
    public void jumpLeisureSubject() {

    }
}
