<script setup lang="ts">

</script>

<template>
  <h3>What do you want to do?</h3>
  <select name="actions" id="actions">
    <option value="sign_doc_only">Sign document Only </option>
    <option value="sign_select_redactable_elems">Sign and select redactable elements</option>
    <option value="sign_redact_doc">Sign and redact document</option>
    <option value="redact_doc">Redact document</option>
    <option value="verify_doc">Verify document</option>
  </select>
</template>

<style scoped>

</style>