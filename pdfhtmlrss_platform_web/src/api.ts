// let apiPrefix = process.env.NODE_ENV === "development" ? "http://localhost:8080" : "/api"
import {RedactionProcess, RedactionProcessAction} from "@/dto/RedactionProcess";
import {Operation} from "@/components/Operations";
import SignatureVerificationReport from "@/dto/SignatureVerificationReport";
import AuthenticationStatus from "@/dto/AuthenticationStatus";
import SignatureDerivationCheckReport from "@/dto/SignatureDerivationCheckReport";

export enum RedactableSignatureOption {
    IMPROVED_COMPATIBILITY = "improved_compatibility",
    SMALLER_FILE = "smaller_size"
}

function getSubsite() : string {
    // Get the current path from the URL
    const path = window.location.pathname;

    // Extract the subsite part
    // eslint-disable-next-line no-useless-escape
    const subsiteMatch = path.match(/^\/([^\/]+)\/?/);
    // return subsiteMatch ? subsiteMatch[0] : window.location.href;
    return subsiteMatch ? subsiteMatch[0] : "/";
}

const apiPrefix : string = `${getSubsite()}api`

export async function checkAuthStatus() : Promise<AuthenticationStatus> {
    return fetch(`${apiPrefix}/auth/status`)
        .then(res => res.json())
}

export function login(user : string, password : string) : Promise<Response> {
    const formData = new FormData()

    formData.set("username", user)
    formData.set("password", password)

    return fetch(`${apiPrefix}/login`, {
        method : 'POST',
        body : formData
    });
}

export function logout() : Promise<Response> {
    return fetch(`${apiPrefix}/logout`, {
        method : 'POST'
    }).then(r => {
        localStorage.clear();
        sessionStorage.clear();
        // caches?.keys()?.then?.(names => {
        //    names.forEach(n => caches.delete(n));
        // });
        return r;
    })
}

export async function signOnly(file : File) : Promise<Blob | undefined> {
    const formData = new FormData()

    formData.set("file", file)
    formData.set("type", file.type)

    const res = await fetch(`${apiPrefix}/sign`, {
        method : 'POST',
        // contentType : "multipart/form-data",
        body : formData
    })
    if(res.ok)
        return res.blob()

    res.json().then(j => {
        throw Error(j.message)
    })
}

export function verifyDocumentDerivation(originalFile : File, redactedFile : File)
    : Promise<SignatureDerivationCheckReport>
{
    const formData = new FormData()

    formData.set("originalFile", originalFile)
    formData.set("redactedFile", redactedFile)

    return fetch(`${apiPrefix}/verify/derivative`, {
        method : 'POST',
        // contentType : "multipart/form-data",
        body : formData
    }).then(r => {
        return r.json().then(j => {
            if(!r.ok)
                throw Error(j.message)
            return j;
        })
    })
}

export async function verifyDocument(file : File) : Promise<SignatureVerificationReport>  {
    const formData = new FormData()

    formData.set("file", file)
    formData.set("type", file.type)

    const res = await fetch(`${apiPrefix}/verify`, {
        method : 'POST',
        // contentType : "multipart/form-data",
        body : formData
    }).then(r => {
        return r.json().then(j => {
            if(!r.ok)
                throw Error(j.message)
            return j;
        })
    })

    const json = await res.json()
    if(res.ok)
        return json as SignatureVerificationReport

    throw Error(json.message)
}

export function submitRedactionProcess(
    file : File,
    operation : Operation
) : Promise<RedactionProcess> {
    const formData = new FormData()
    formData.set("file", file)
    formData.set("redactionTask", operation.toString())

    return fetch(`${apiPrefix}/sign/prepare`, {
        method : 'POST',
        body : formData
    })
    .then(r => {
        return r.json().then(j => {
            if(!r.ok)
                throw Error(j.message)
            return j;
        })
    })
    .then(j => j as RedactionProcess)
}

export function getRedactionProcess(
    processId : string
) : Promise<RedactionProcess> {
    return fetch(`${apiPrefix}/sign/${processId}`, {
        method : 'GET'
    })
    .then(r => r.json())
    .then(j => j as RedactionProcess)
}

export function cancelRedactionProcess(
    processId : string
) : Promise<Response> {
    return fetch(`${apiPrefix}/sign/${processId}`, {
        method : 'DELETE'
    })
}

export async function finishRedactionProcess(
    processId : string,
    elementsToRedact : string[]
) : Promise<Blob | undefined> {
    const formData = new FormData()
    formData.set("elementsToRedact", elementsToRedact.toString())

    const res = await fetch(`${apiPrefix}/sign/${processId}`, {
        method : 'POST',
        body : formData
    })
    if(res.ok)
        return res.blob()

    res.json().then(j => {
        throw Error(j.message)
    })
}

export function getRSSSignatureDebug(
    pdfFile : File
) :Promise<Response> {
    const formData = new FormData()
    formData.set("file", pdfFile)
    return fetch(`${apiPrefix}/debug/rss`, {
        method : 'POST',
        body : formData
    })
}


export function getTemporaryFile(
    tmpFileName : string
) : Promise<Response> {

    return fetch(getTemporaryFileURL(tmpFileName))
}

export function getTemporaryFileURL(
    tmpFileName : string
) : string {

    return `${apiPrefix}/tmp/${tmpFileName}`
}

export function getRedactJsScriptUrl() {
    return `${getSubsite()}redact.js`
}

export function getRedactCssSheetUrl() {
    return `${getSubsite()}redact.css`
}