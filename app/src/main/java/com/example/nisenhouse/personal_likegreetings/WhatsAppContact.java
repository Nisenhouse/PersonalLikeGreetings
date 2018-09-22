package com.example.nisenhouse.personal_likegreetings;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;

public class WhatsAppContact implements Observable {
    private String number;
    private String fullName;
    private String name;
    private boolean checked;

    private PropertyChangeRegistry propertyChangeRegistry = new PropertyChangeRegistry();

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        propertyChangeRegistry.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        propertyChangeRegistry.remove(callback);
    }

    public WhatsAppContact(String number, String fullName, String name) {
        this.number = number;
        this.fullName = fullName;
        this.name = name;
        this.checked = false;
    }

    @Bindable
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
        propertyChangeRegistry.notifyChange(this, BR.number);
    }

    @Bindable
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        propertyChangeRegistry.notifyChange(this, BR.fullName);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        propertyChangeRegistry.notifyChange(this, BR.name);
    }

    @Bindable
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        propertyChangeRegistry.notifyChange(this, BR.checked);
    }

    @Override
    public String toString() {
        return "full name: " + this.fullName + "; name " + this.name + "; number: " + this.number;
    }
}
