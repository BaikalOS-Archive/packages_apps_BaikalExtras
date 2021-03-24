#  Copyright (C) 2015 The Android Open Source Project
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets


LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.core_core \
    androidx.preference_preference \
    androidx.appcompat_appcompat \
    androidx.cardview_cardview \
    androidx.recyclerview_recyclerview \
    com.google.android.material_material \
    com.aicp.settings.resources \
    AicpGear-preference \
    AicpGear-util

# Apache http for stats
LOCAL_JAVA_LIBRARIES := org.apache.http.legacy

LOCAL_USE_AAPT2 := true

LOCAL_PACKAGE_NAME := BaikalExtras

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PRODUCT_MODULE := true

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))


