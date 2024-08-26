// let apiPrefix = process.env.NODE_ENV === "development" ? "http://localhost:8080" : "/api"
import {RedactionProcess, RedactionProcessAction} from "@/dto/RedactionProcess";

function getSubsite() : string {
    // Get the current path from the URL
    const path = window.location.pathname;

    // Extract the subsite part
    // eslint-disable-next-line no-useless-escape
    const subsiteMatch = path.match(/^\/([^\/]+)\/?/);
    return subsiteMatch ? subsiteMatch[0] : "/";
}

const apiPrefix : string = `${getSubsite()}api`

export function testApi() : Promise<Response> {
    return fetch(`${apiPrefix}/test`)
}

export function signOnly(file : File)  {
    const formData = new FormData()

    formData.set("file", file)
    formData.set("type", file.type)

    return fetch(`${apiPrefix}/sign`, {
        method : 'POST',
        // contentType : "multipart/form-data",
        body : formData
    })
}

export function submitRedactionProcess(
    file : File,
    action : RedactionProcessAction
) : Promise<RedactionProcess> {
    const formData = new FormData()
    formData.set("file", file)
    formData.set("redactionTask", action.toString())

    return fetch(`${apiPrefix}/sign/prepare`, {
        method : 'POST',
        body : formData
    })
    .then(r => r.json())
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

export function finishRedactionProcess(
    processId : string,
    elementsToRedact : string[]
) : Promise<Response> {
    const formData = new FormData()
    formData.set("elementsToRedact", elementsToRedact.toString())

    return fetch(`${apiPrefix}/sign/${processId}`, {
        method : 'POST',
        body : formData
    })
}

export function getTemporaryFile(
    tmpFileName : string
) : Promise<Response> {

    return fetch(`${apiPrefix}/tmp/${tmpFileName}`)
}