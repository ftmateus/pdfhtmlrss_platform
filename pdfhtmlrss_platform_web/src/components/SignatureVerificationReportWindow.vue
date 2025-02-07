<script setup lang="ts">

import SignatureVerificationReport from "@/dto/SignatureVerificationReport";
import { defineProps } from "vue"

const props = defineProps<{
  closeWindow : Function;
  report : SignatureVerificationReport
}>();

const generalSignatureProperties = [
  ["Issued by: ", () => props.report.issuedBy],
  ["Signature Date: ", () => props.report.signatureDate],
]

const padesSignatureProperties = [
  ["Algorithm: ", () => props.report.padesAlgorithm],
  ["Signature violated: ", () => !props.report.rssNotModified],
]

const rssSignatureProperties = [
  ["Algorithm: ", () => props.report.rssAlgorithm],
  ["Signature violated: ", () => !props.report.rssNotModified],
]

function renderSignatureProperties(sigProps : Array<[string, Function]>) {
    return sigProps.map(([label, value]) => {
      return `
          <div>
              <label>${label}</label>
              ${value()}
          </div>
      `
    }).join("\n")
}

// const redactedElemsTextBoxRef = ref<String>()

</script>

<template>
  <dialog open>
    <div v-html="renderSignatureProperties(generalSignatureProperties)"/>
    <div>
      <h4>PAdES</h4>
      <div v-html="renderSignatureProperties(padesSignatureProperties)"/>
    </div>
<!--    <label v-else>No PAdES Signature is present</label>-->
    <div v-if="report.hasRSSSignature">
      <h4>RSS</h4>
      <div v-html="renderSignatureProperties(rssSignatureProperties)"/>
    </div>
    <label v-else>No RSS Signature is present</label>
    <button @click="closeWindow">Close</button>
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