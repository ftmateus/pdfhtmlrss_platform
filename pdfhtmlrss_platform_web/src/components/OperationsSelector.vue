<script setup lang="ts">

import {Operation, opToTitle} from "@/components/Operations";
import { defineModel } from 'vue';

const currentOperation = defineModel()

</script>

<template>
  <h3>What do you want to do with your document?</h3>
  <div style="display: flex; justify-content: space-around; width: 100%">
    <button
        v-bind:key="a" v-for="a in Object.values(Operation)"
        @click="() => currentOperation = a"
        :class="currentOperation === a ? 'selected' : ''"
        style="width: 100px; border-style: solid; font-size: 12px; height: 52px"
    >
      {{ opToTitle(a) }}
    </button>
  </div>
<!--  <select-->
<!--      name="actions"-->
<!--      id="actions"-->
<!--      @change="handleActionChange"-->
<!--      v-model="currentOperation"-->
<!--  >-->
<!--    <option v-bind:key="a" v-for="a in Object.values(Operation)" :value="a" >-->
<!--        {{opToTitle(a)}}-->
<!--    </option>-->
<!--  </select>-->
</template>
<style scoped>
.selected,.selected:hover {
  background-color: cadetblue;
}
</style>