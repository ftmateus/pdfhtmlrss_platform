<template>
  <div id="warning" style="z-index: 1">
    Attention: This is a proof of concept application intended solely for demonstration and research purposes.
    Never use this in real world applications!
  </div>
  <div class="app-view" :style="{justifyContent: documentViewToggle ? 'space-between' : 'center'}">
    <div class="control-view" >
      <img alt="Vue logo" src="./assets/logo.png" style="margin-top: 20px; height: 200px;"/>
      <h1>PDF HTML Redactable Signatures Platform</h1>
      <RouterView
          @open-document-view="handleOpenDocumentView"
          @close-document-view="handleCloseDocumentView"
      />
    </div>
    <div class="document-view"
         v-if="documentViewToggle" ref="documentView"
         :style="{width: documentViewWidth ?? '100%'}"
    >
<!--      <div class="separator"-->
<!--           @touchdown="handleSeparatorResize"-->
<!--           @drag="handleSeparatorResize"-->
<!--      />-->
      <div class="separator"/>
      <iframe
          class="document-view" v-if="documentViewToggle"
          :src="documentViewUrl"
          ref="documentViewIframe"
          width="100%"
          @load="injectRedactScript"
      />
    </div>
<!--    <div :style="{height : '100%'}" v-if="viewToggle">-->
<!--    </div>-->
  </div>
</template>

<script setup lang="ts">

import { ref } from 'vue';
import {getRedactJsScriptUrl, getRedactJsStyleUrl} from "@/api";

import '@/assets/style.css'

const documentViewToggle = ref<boolean>(false);
const documentViewUrl = ref<string>("");
const documentViewWidth = ref<number | undefined>(undefined);
const documentView = ref(null)
const documentViewIframe = ref(null)

function handleOpenDocumentView(url : string) {
  documentViewUrl.value = url;
  documentViewToggle.value = true;
}

function handleCloseDocumentView() {
  documentViewToggle.value = false;
  documentViewUrl.value = "";
}

function injectRedactScript() {
  if(!documentViewIframe.value)
    return;

  const redactScript = document.createElement("script");
  redactScript.src =  getRedactJsScriptUrl()
  documentViewIframe.value.contentDocument.body.appendChild(redactScript);

  const redactStyle = document.createElement("link");
  redactStyle.rel = "stylesheet";
  redactStyle.type = "text/css";
  redactStyle.href =  getRedactJsStyleUrl()
  documentViewIframe.value.contentDocument.body.appendChild(redactStyle);
}

// function handleSeparatorResize(e : MouseEvent) {
//   if(!documentView.value)
//     return;
//
//   // console.log(documentView.value.clientWidth);
//   console.log(e.movementX);
//   documentViewWidth.value = documentView.value.clientWidth + e.movementX;
// }

</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#warning {
  width: 100%;
  background-color: yellow;
  top: 0;
  left: 0;
  position: absolute;
  text-align: center;
}

.app-view {
  position: absolute;
  display: flex;
  justify-content: space-between;
  height: 100%;
  width: 100%;
  flex-direction: row;
}

.control-view {
  display: flex;
  flex-direction: column;
  justify-content: start;
  align-items: center;
  background-color: aliceblue;
  padding-left: 30px;
  padding-right: 30px;
}

.separator {
  width: 20px;
  background-color: darkslategrey;
  cursor: col-resize;
}

.document-view {
  height: 100%;
  max-width: 100%;
  flex-grow: 1;
  display: flex;
  flex-direction: row;
}

.document-view iframe {
  position: relative;
  height: 100%;
  width: 100%;
  min-width: 20%;
}
</style>
