// let apiPrefix = process.env.NODE_ENV === "development" ? "http://localhost:8080" : "/api"
import {RedactionProcess, RedactionProcessAction} from "@/dto/RedactionProcess";
import {Operation} from "@/components/Operations";

function getSubsite() : string {
    // Get the current path from the URL
    const path = window.location.pathname;

    // Extract the subsite part
    // eslint-disable-next-line no-useless-escape
    const subsiteMatch = path.match(/^\/([^\/]+)\/?/);
    return subsiteMatch ? subsiteMatch[0] : "/";
}

const apiPrefix : string = `${getSubsite()}api`

export function checkAuthStatus() : Promise<Response> {
    return fetch(`${apiPrefix}/auth/status`)
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
    });
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
    operation : Operation
) : Promise<RedactionProcess> {
    const formData = new FormData()
    formData.set("file", file)
    formData.set("redactionTask", operation.toString())

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

    return fetch(getTemporaryFileURL(tmpFileName))
}

export function getTemporaryFileURL(
    tmpFileName : string
) : string {

    return `${apiPrefix}/tmp/${tmpFileName}`
}