// let apiPrefix = process.env.NODE_ENV === "development" ? "http://localhost:8080" : "/api"
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

export async function signOnly(file : File)  {
    const formData = new FormData()

    formData.set("file", file)
    formData.set("type", file.type)

    return await fetch(`${apiPrefix}/sign`, {
        method : 'POST',
        // contentType : "multipart/form-data",
        body : formData
    })
}