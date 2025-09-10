
let redactionProcessId = window.location.pathname.split("/")
    .pop().split(".")[0]

function getSubsite() {
    // Get the current path from the URL
    const path = window.location.pathname;

    // Extract the subsite part
    // eslint-disable-next-line no-useless-escape
    const subsiteMatch = path.match(/^\/([^\/]+)\/?/);
    // return subsiteMatch ? subsiteMatch[0] : window.location.href;
    return subsiteMatch ? subsiteMatch[0] : "/";
}

const apiPrefix = `${getSubsite()}`


localStorage.setItem("elementsToRedact", JSON.stringify([]))

let signatureNode = document.getElementsByTagName("signature")[0]

let allowedRedactableElems = []

let redactionProcess = null;

fetch(`${apiPrefix}/sign/${redactionProcessId}`, {
    method : 'GET'
})
.then(r => r.json())
.then(rp => {
    redactionProcess = rp
    console.log(redactionProcess)

    if(!signatureNode || redactionProcess.action !== "REDACT")
        return;

    let pointers = signatureNode.getElementsByTagName("pointer")
    for (let i = 0; i < pointers.length; i++) {
        let p = pointers[i]
        let uriAttr = p.attributes['uri']
        if (!uriAttr || !uriAttr.nodeValue.startsWith("#xpath"))
            continue

        let xpathUri = uriAttr.nodeValue
            .match(/#xpath\((.*)\)/)[1]

        let elem = document.evaluate(xpathUri, document)?.iterateNext();

        allowedRedactableElems.push(elem)
    }

    if (allowedRedactableElems.length === 0 && redactionProcess.action === "REDACT")
        allowedRedactableElems = null;

    allowedRedactableElems?.forEach(elem => {
        elem.setAttribute("redactable", "")
    })
});

window.onload = function() {
    const anchors = document.getElementsByTagName("a");
    for (let i = 0; i < anchors.length; i++) {
        anchors[i].onclick = function() { return false;};
    }
};

window.onclick = function (e) {
    let selectedElem = e.target;

    if(selectedElem.tagName === "BODY")
        return

    if(selectedElem?.getAttribute("redacted") == null)
        return

    selectedElem.removeAttribute("redacted");

    let elementsToRedact = JSON.parse(localStorage.getItem("elementsToRedact"))
    let elementXpath = getElementXPath(selectedElem);
    elementsToRedact = elementsToRedact.filter(a => a !== elementXpath)
    localStorage.setItem("elementsToRedact", JSON.stringify(elementsToRedact));
}

window.ondblclick = function(e) {
    let selectedElem = e.target;

    if(selectedElem.tagName === "BODY")
        return

    if(selectedElem?.getAttribute("redacted") !== null)
        return

    if(!allowedRedactableElems?.includes(selectedElem)
    && redactionProcess.action === "REDACT")
        return;

    selectedElem.setAttribute("redacted", "");
    //clears all text selections
    window.getSelection().empty();

    let elementsToRedact = JSON.parse(localStorage.getItem("elementsToRedact"))

    let elementXpath = getElementXPath(selectedElem);
    console.log(elementXpath);

    elementsToRedact.push(elementXpath);
    localStorage.setItem("elementsToRedact", JSON.stringify(elementsToRedact));
}

function getElementXPath(element) {
    if (!element) return null;

    const parts = [];
    while (element && element.nodeType === Node.ELEMENT_NODE) {
        let index = 1;
        let sibling = element.previousElementSibling;

        // Count previous siblings of the same tag
        while (sibling) {
            if (sibling.tagName === element.tagName) index++;
            sibling = sibling.previousElementSibling;
        }

        const tagName = element.tagName.toLowerCase();
        parts.unshift(`${tagName}[${index}]`); // Push XPath part to array
        element = element.parentElement;
    }

    return `/${parts.join("/")}`;
}