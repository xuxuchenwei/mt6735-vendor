# -*- mode: makefile -*-
# Copyright (C) 2011 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Definitions for installing Certificate Authority (CA) certificates
#

define all-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -type f) \
 )
endef

# $(1): module name
# $(2): source file
# $(3): destination directory
define include-prebuilt-with-destination-directory
include $$(CLEAR_VARS)
LOCAL_MODULE := $(1)
LOCAL_ADDITIONAL_DEPENDENCIES := $(LOCAL_PATH)/CaCerts_supl.mk
LOCAL_MODULE_STEM := $(notdir $(2))
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(3)
LOCAL_SRC_FILES := $(2)
include $$(BUILD_PREBUILT)
endef

cacerts_supl := $(call all-files-under,files/cacerts_supl)

cacerts_supl_target_directory := $(TARGET_OUT)/etc/security/cacerts_supl
$(foreach cacert, $(cacerts_supl), $(eval $(call include-prebuilt-with-destination-directory,target-cacert-supl-$(notdir $(cacert)),$(cacert),$(cacerts_supl_target_directory))))
cacerts_supl_target := $(addprefix $(cacerts_supl_target_directory)/,$(foreach cacert,$(cacerts_supl),$(notdir $(cacert))))
.PHONY: cacerts_supl_target
cacerts_supl: $(cacerts_supl_target)

# This is so that build/target/product/core.mk can use cacerts_supl in PRODUCT_PACKAGES
ALL_MODULES.cacerts_supl.INSTALLED := $(cacerts_supl_target)

