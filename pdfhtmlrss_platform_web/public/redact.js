
let redactionProcess = window.location.pathname.split("/")
    .pop().split(".")[0]

localStorage.setItem("elementsToRedact", JSON.stringify([]))

let signatureNode = document.getElementsByTagName("signature")[0]

let allowedRedactableElems = []

if(signatureNode) {
    let pointers = signatureNode.getElementsByTagName("pointer")
    for(let i = 0; i < pointers.length; i++) {
        let p = pointers[i]
        let uriAttr = p.attributes['uri']
        if(!uriAttr || !uriAttr.nodeValue.startsWith("#xpath"))
            continue

        let xpathUri = uriAttr.nodeValue
            .match(/#xpath\((.*)\)/)[1]

        let elem = document.evaluate(xpathUri, document)?.iterateNext();

        allowedRedactableElems.push(elem)
    }
}

allowedRedactableElems.forEach(elem => {
    elem.setAttribute("redactable", "")
})

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
    console.log(elementXpath);
    elementsToRedact = elementsToRedact.filter(a => a !== elementXpath)
    localStorage.setItem("elementsToRedact", JSON.stringify(elementsToRedact));
}

window.ondblclick = function(e) {
    let selectedElem = e.target;

    if(selectedElem.tagName === "BODY")
        return

    if(selectedElem?.getAttribute("redacted") !== null)
        return

    if(allowedRedactableElems.length > 0
    && !allowedRedactableElems.includes(selectedElem))
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