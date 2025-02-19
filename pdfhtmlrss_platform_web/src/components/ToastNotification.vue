<script setup lang="ts">

import {defineProps} from "vue"
import {ToastType} from "@/components/ToastNotificationType";

const props = defineProps<{
  type: ToastType,
  // message: string,
  detailsClick? : Function,
  dismissClick? : Function,
}>();

function toastTypeToCSSClass() {
  switch (props.type) {
    case ToastType.SUCCESS : return 'toast-notification-success'
    case ToastType.ERROR : return 'toast-notification-error'
    case ToastType.INFO: return 'toast-notification-info'
    default: return ''
  }
}

</script>

<template>
  <div :class="['toast-notification', toastTypeToCSSClass()]">
    <slot></slot>
    <div style="display: flex; align-items: center; justify-content: center; gap: 1rem;">
      <a v-if="detailsClick" href="#" @click="detailsClick">Details</a>
      <a v-if="dismissClick" href="#" @click="dismissClick">Dismiss</a>
    </div>
  </div>
</template>

<style scoped>
    .toast-notification {
        border-radius: 10px;
        padding: 8px;
        border-color: black;
        border-width: thin;
        border-style: solid;
        align-self: center;
        max-width: 400px;
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