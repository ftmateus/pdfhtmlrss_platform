<script setup lang="ts">

import {ref} from "vue";
import SignatureVerificationReport from "@/dto/SignatureVerificationReport";

defineProps<{
  closeWindow : Function;
  report : SignatureVerificationReport
}>();

// const redactedElemsTextBoxRef = ref<String>()

</script>

<template>
  <dialog open>
    <div>
      <label>Issued by:</label>
      {{ report.issuedBy }}
    </div>
    <div>
      <label>Signature date:</label>
      {{ report.signatureDate }}
    </div>
    <div>
      <h4>PAdES</h4>
      <div>
        <label>Algorithm:</label>
        {{ report.padesAlgorithm }}
      </div>
      <div>
        <label>Signature violated:</label>
        {{ !report.padesNotModified }}
      </div>
    </div>
    <div>
      <h4>RSS</h4>
      <div>
        <label>Algorithm:</label>
        {{ report.rssAlgorithm }}
      </div>
      <div>
        <label>Signature violated:</label>
        {{ !report.rssNotModified }}
      </div>
    </div>
    <button @click="closeWindow">Close</button>
  </dialog>
</template>

<style scoped>
  dialog::backdrop {
    background: rgba(0, 0, 0, 0.5);
  }

</style>