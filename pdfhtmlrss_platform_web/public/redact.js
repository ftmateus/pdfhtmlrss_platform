
let redactionProcess = window.location.pathname.split("/")
    .pop().split(".")[0]

localStorage.setItem("elementsToRedact", JSON.stringify([]))

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

    selectedElem.setAttribute("redacted", "");
    //clears all text selections
    window.getSelection().empty();

    let elementsToRedact = JSON.parse(localStorage.getItem("elementsToRedact"))
    elementsToRedact.push(getElementXPath(selectedElem));
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