<script setup lang="ts">
import {defineEmits} from "vue";

const emit = defineEmits(['close-window'])


</script>

<template>
  <dialog open style="padding-right: 32px; padding-left: 32px;">
    <slot></slot>
    <button @click="() => emit('close-window')" style="margin-top: 24px">Close</button>
  </dialog>
</template>

<style scoped>
dialog::backdrop {
  background: rgba(0, 0, 0, 0.5);
}

dialog {
  border-radius: 30px;
}

closeBtn {
  right: 0px !important;
}
</style>