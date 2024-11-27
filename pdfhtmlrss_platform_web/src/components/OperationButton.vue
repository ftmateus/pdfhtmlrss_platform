<script setup lang="ts">
import { defineProps } from 'vue';
import { opToButtonTitle } from "@/components/Operations";

defineProps<{
  operation : {
    type: String,
    required: true
  }
}>();
</script>

<template>
  <button>{{opToButtonTitle($props.operation)}}</button>
</template>

<style scoped>

</style>