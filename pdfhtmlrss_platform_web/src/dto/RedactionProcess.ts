export type RedactionProcess = {
    taskId : string,
    userId : string,
    fileType : string,
    tmpPdfFile : string | null,
    tmpHtmlFile : string,
    expires : number,
    action : RedactionProcessAction
};

export enum RedactionProcessAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}