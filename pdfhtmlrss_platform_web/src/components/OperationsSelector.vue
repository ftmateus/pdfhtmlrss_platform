<script setup lang="ts">

import {Operation, opToTitle} from "@/components/Operations";
import {ref, Ref} from "vue";

// const actionsSelectorRef: Ref<HTMLSelectElement> = ref(null)

let currentOperation = Operation.SIGN_ONLY

const props = defineProps({
  // setAction : {
  //   type : Function,
  //   required : true
  // }
})

const operationModel = defineModel()

function handleActionChange(e : Event) {
  operationModel.value = currentOperation
}

</script>

<template>
  <h3>What do you want to do?</h3>
  <select
      ref="actionsSelectorRef"
      name="actions"
      id="actions"
      @change="handleActionChange"
      v-model="currentOperation"
  >
    <option v-for="a in Object.values(Operation)" :value="a">
        {{opToTitle(a)}}
    </option>
  </select>
</template>

<style scoped>



</style>