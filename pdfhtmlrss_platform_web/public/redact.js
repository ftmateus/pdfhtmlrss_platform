
const redactedElementsPreviousStyles = new Map();

let redactionProcess = window.location.pathname.split("/")
    .pop().split(".")[0]

localStorage.setItem("elementsToRedact", JSON.stringify([]))

console.log(redactionProcess)

window.onclick = function(e) {
    let selectedElem = e.target;
    if(selectedElem?.getAttribute("redacted") !== null) {
        let oldStyle = redactedElementsPreviousStyles.get(selectedElem);
        for (const styleProperty in oldStyle) {
            selectedElem.style[styleProperty] = oldStyle[styleProperty];
        }
        selectedElem.removeAttribute("redacted");
        redactedElementsPreviousStyles.delete(selectedElem);

        let elementsToRedact = JSON.parse(localStorage.getItem("elementsToRedact"))
        let elementXpath = getElementXPath(selectedElem);
        elementsToRedact = elementsToRedact.filter(a => a !== elementXpath)
        localStorage.setItem("elementsToRedact", JSON.stringify(elementsToRedact));
    } else {
        selectedElem.setAttribute("redacted", "");
        if(selectedElem.tagName === "IMG")
            redactImageElement(selectedElem);
        else
            redactTextElement(selectedElem);
        // let elementXpath = getElementXPath(selectedElem)

        let elementsToRedact = JSON.parse(localStorage.getItem("elementsToRedact"))
        elementsToRedact.push(getElementXPath(selectedElem));
        localStorage.setItem("elementsToRedact", JSON.stringify(elementsToRedact));
    }
}

function redactTextElement(element) {
    redactedElementsPreviousStyles.set(element, {
        color : element.style.color,
        backgroundColor : element.style.backgroundColor,
    });
    element.style.color = "black";
    element.style.backgroundColor = "black";
}

function redactImageElement(element) {
    redactedElementsPreviousStyles.set(element, {
        filter : element.style.filter,
    });
    element.style.filter = 'blur(25px)';
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