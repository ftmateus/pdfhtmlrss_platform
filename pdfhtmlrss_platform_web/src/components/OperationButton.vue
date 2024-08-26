<script setup lang="ts">

import {Operation, opToButtonTitle} from "@/components/Operations";

const props = defineProps({
  operation : {
    type : String,
    required : true
  }
})

</script>

<template>
  <button>{{opToButtonTitle(operation)}}</button>
</template>

<style scoped>

</style>