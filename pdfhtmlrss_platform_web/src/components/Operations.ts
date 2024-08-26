import { def } from "@vue/shared"

export enum Operation {
    SIGN_ONLY = "sign_only",
    SIGN_SELECT_REDACTABLE_ELEMS = "sign_select_redactable_elems",
    REDACT = "redact",
    VERIFY = "verify"
}

export function opToTitle(operation : Operation) : string {
    switch (operation) {
        case Operation.SIGN_ONLY:
            return "Sign document only"
        case Operation.REDACT :
            return "Redact document"
        case  Operation.SIGN_SELECT_REDACTABLE_ELEMS:
            return "Sign and select redactable elements"
        case Operation.VERIFY :
            return  "Verify document"
        default : return ""
    }
}

export function opToButtonTitle(operation : Operation | String) {
    switch (operation) {
        case Operation.SIGN_ONLY:
            return "Sign"
        case Operation.REDACT:
            return "Submit"
        case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
            return "Submit"
        case Operation.VERIFY :
            return  "Verify"
        default : return ""
    }
}

// export default {
//     SIGN_ONLY : {
//         msg : "Sign document only"
//     },
//     SIGN_SELECT_REDACTABLE_ELEMS : {
//         msg : "Sign and select redactable elements"
//     },
//     REDACT : {
//         msg : "Redact document"
//     },
//     VERIFY : {
//         msg : "Verify document"
//     }
// }