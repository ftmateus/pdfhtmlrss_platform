<script setup lang="ts">
import { defineProps } from 'vue';
import { opToButtonTitle } from "@/components/Operations";

defineProps<{
  operation : {
    type: string,
    required: true
  },
  noFileSelected : {
    type : boolean,
    required : true
  }
}>();
</script>

<template>
  <button :disabled="$props.noFileSelected">{{opToButtonTitle($props.operation)}}</button>
</template>

<style scoped>

</style>