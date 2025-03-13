<script setup lang="ts">

import SignatureVerificationReport from "@/dto/SignatureVerificationReport";
import { defineProps } from "vue"

const props = defineProps<{
  report : SignatureVerificationReport,
  title : String,
}>();

const generalSignatureProperties = [
  ["Issued by: ", () => props.report.issuedBy],
  ["Signature date: ", () => props.report.signatureDate],
  ["Has external signatures: ", () => props.report.hasExternalSignatures],
]

const externalSignaturesProperties = [
  ["External signatures violated: ",
      () => props.report.externalSignaturesViolated,
      () => props.report.externalSignaturesViolated ? "red" : "green"
  ],
]

const padesSignatureProperties = [
  ["Algorithm: ", () => props.report.rssPAdESAlgorithm],
  ["Signature violated: ",
      () => props.report.rssPAdESViolated,
      () => props.report.rssPAdESViolated ? "red" : "green"
  ],
]

const rssSignatureProperties = [
  ["Algorithm: ", () => props.report.rssXMLAlgorithm],
  ["Signature violated: ",
      () => props.report.rssXMLViolated,
      () => props.report.rssXMLViolated ? "red" : "green"
  ],
]

// const redactedElemsTextBoxRef = ref<String>()

</script>

<template>
  <div>
    <h2>{{ title }}</h2>
    <div v-for="[label, value] in generalSignatureProperties" v-bind:key="label">
      <label>{{ label }}</label>
      {{ value() }}
    </div>
    <div v-if="report.hasExternalSignatures">
      <div v-for="[label, value, propColor] in externalSignaturesProperties" v-bind:key="label">
        <label>{{ label }}</label>
        <label :style="{color: propColor?.() ?? 'black', fontWeight : 'bold'}">{{ value() }}</label>
      </div>
    </div>
    <div v-if="report.hasRSSPAdESSignature">
      <h4>PAdES</h4>
      <div v-for="[label, value, propColor] in padesSignatureProperties" v-bind:key="label">
        <label>{{ label }}</label>
        <label :style="{color: propColor?.() ?? 'black', fontWeight : 'bold'}">{{ value() }}</label>
      </div>
    </div>
    <h5 v-else style="color: #b9b900">Document was not signed by this tool!</h5>
    <!--    <label v-else>No PAdES Signature is present</label>-->
    <div v-if="report.hasRSSPAdESSignature && report.hasRSSXMLSignature">
      <h4>RSS</h4>
      <div v-for="[label, value, propColor] in rssSignatureProperties" v-bind:key="label">
        <label>{{ label }}</label>
        <label :style="{color: propColor?.() ?? 'black', fontWeight : 'bold'}">{{ value() }}</label>
      </div>
    </div>
    <h5 v-else-if="report.hasRSSPAdESSignature" style="color: #b9b900">No Redactable Signature is present!</h5>

  </div>
</template>

<style scoped>

</style>