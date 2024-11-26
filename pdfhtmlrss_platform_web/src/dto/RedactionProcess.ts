export type RedactionProcess = {
    taskId : string,
    userId : string,
    fileType : string,
    tmpPdfFile : string | null,
    tmpHtmlFile : string,
    action : RedactionProcessAction
    // expires : number,
};

export enum RedactionProcessAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}