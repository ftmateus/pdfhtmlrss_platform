<script setup lang="ts">

import {Operation, opToTitle} from "@/components/Operations";
import { defineModel } from 'vue';


const operationModel = defineModel()
let currentOperation = operationModel.value

function handleActionChange() {
  operationModel.value = currentOperation
}

</script>

<template>
  <h3>What do you want to do?</h3>
  <select
      name="actions"
      id="actions"
      @change="handleActionChange"
      v-model="currentOperation"
  >
    <option v-bind:key="a" v-for="a in Object.values(Operation)" :value="a" >
        {{opToTitle(a)}}
    </option>
  </select>
</template>
<style scoped>
</style>