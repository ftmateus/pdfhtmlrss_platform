<script setup lang="ts">

import {defineProps} from "vue"
import {ToastType} from "@/components/ToastNotificationType";

const props = defineProps<{
  type: ToastType,
  message: string,
  detailsClick? : Function,
  dismissClick : Function,
}>();

function toastTypeToCSSClass() {
  switch (props.type) {
    case ToastType.SUCCESS : return 'toast-notification-success'
    case ToastType.ERROR : return 'toast-notification-error'
    default: return ''
  }
}

</script>

<template>
  <div :class="['toast-notification', toastTypeToCSSClass()]">
    <label>{{ message }}</label>
    <div>
      <a v-if="detailsClick" href="#" @click="detailsClick">Details</a>
      &nbsp;
      <a href="#" @click="dismissClick">Dismiss</a>
    </div>
  </div>
</template>

<style scoped>
    .toast-notification {
        border-radius: 10px;
        padding: 8px;
        border-color: black;
        border-width: medium;
    }

    .toast-notification-success {
      background-color: #7cec7c;
    }

    .toast-notification-error {
      background-color: #ec6c6c;
    }

    .toast-notification-info {
      background-color: #428eff;
    }
</style>