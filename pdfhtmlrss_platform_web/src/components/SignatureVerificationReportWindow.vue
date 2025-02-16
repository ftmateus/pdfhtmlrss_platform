<script setup lang="ts">

import SignatureVerificationReport from "@/dto/SignatureVerificationReport";
import { defineProps, defineEmits } from "vue"

const props = defineProps<{
  report : SignatureVerificationReport
}>();
const emit = defineEmits(['close-window'])

const generalSignatureProperties = [
  ["Issued by: ", () => props.report.issuedBy],
  ["Signature Date: ", () => props.report.signatureDate],
]

const padesSignatureProperties = [
  ["Algorithm: ", () => props.report.padesAlgorithm],
  ["Signature violated: ",
      () => !props.report.padesNotModified,
      () => props.report.padesNotModified ? "green" : "red"
  ],
]

const rssSignatureProperties = [
  ["Algorithm: ", () => props.report.rssAlgorithm],
  ["Signature violated: ",
      () => !props.report.rssNotModified,
      () => props.report.rssNotModified ? "green" : "red"
  ],
]

// const redactedElemsTextBoxRef = ref<String>()

</script>

<template>
  <dialog open>
    <div v-for="[label, value] in generalSignatureProperties" v-bind:key="label">
      <label>{{ label }}</label>
      {{ value() }}
    </div>
    <div>
      <h4>PAdES</h4>
      <div v-for="[label, value, propColor] in padesSignatureProperties" v-bind:key="label">
        <label>{{ label }}</label>
        <label :style="{color: propColor?.() ?? 'black'}">{{ value() }}</label>
      </div>
    </div>
<!--    <label v-else>No PAdES Signature is present</label>-->
    <div v-if="report.hasRSSSignature">
      <h4>RSS</h4>
      <div v-for="[label, value, propColor] in rssSignatureProperties" v-bind:key="label">
        <label>{{ label }}</label>
        <label :style="{color: propColor?.() ?? 'black'}">{{ value() }}</label>
      </div>
    </div>
    <h5 v-else style="color: #b9b900">No Redactable Signature is present!</h5>
    <button @click="() => emit('close-window')">Close</button>
  </dialog>
</template>

<style>
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