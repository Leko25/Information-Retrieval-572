// Control radio buttons
const buttonIDs = ["luceneRadio", "pageRankRadio"];

function checkRadio(id) {
    var radio = $(`#${id}`);

    radio.prop("checked", true);

    if (id === buttonIDs[0]) {
        $(`#${buttonIDs[1]}`).prop("checked", false);
    }
    else if (id === buttonIDs[1]) {
        $(`#${buttonIDs[0]}`).prop("checked", false);
    }
}

function reSubmit(query) {
    $("input[name=search]").val(query);
    submitSearch(true);
}

var someData = ["google", "amazon", "xim", "netflix"];

//AutoComplete Section
document.getElementById("search-input").addEventListener("input", (e) => {
    var val = $("input[name=search]").val();
    
    /* Close any already open lists of autocompleted values */
    closeAllLists();
    if (!val) {
        return false;
    }

    getSuggestions(val);
});

function getSuggestions(query) {
    var data = {
        "q": query
    };

    fetch(`${window.origin}/autocomplete`, {
        method: "POST",
        credentials: "include",
        body: JSON.stringify(data),
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => {
        if (response.status !== 200) {
            console.log(`Response status was not 200: ${response.status}`);
            return
        }
        response.json().then((data) => {
            var suggestions = data["suggestions"];
            var a = document.createElement("DIV");
            a.setAttribute("id", $("input[name=search]").attr("id") + "-autocomplete-list");
            a.setAttribute("class", "autocomplete-items");
            document.getElementById("search-input").parentNode.appendChild(a);
            for (var i = 0; i < suggestions.length; i++) {
                var b = document.createElement("DIV");
                b.innerHTML = "<strong>" + suggestions[i].substr(0, query.length) + "</strong>";
                b.innerHTML += suggestions[i].substr(query.length);
                b.innerHTML += "<input type='hidden' value='" + suggestions[i] + "'>";
                b.addEventListener("click", (e) => {
                    document.getElementById("search-input").value = e.target.getElementsByTagName("input")[0].value;
                    closeAllLists();
                })
                a.appendChild(b);
            }
        });
    });
}

function closeAllLists(elmnt) {
    /* Close all autocomplete list in the document,
    except the one passed as an argument */
    var x = document.getElementsByClassName("autocomplete-items");
    for (var i = 0; i < x.length; i++) {
        if (elmnt != x[i] && elmnt != $("input[name=search]")) {
            x[i].parentNode.removeChild(x[i]);
        }
    }
}

document.addEventListener("click", (e) => {
    closeAllLists(e.target);
});

// AJAX Request to FLASK Server
function submitSearch(sumbitFlag) {
    var formData = {
        "search": $("input[name=search]").val(),
        "algo": $("input[type=radio]:checked").val(),
        "category": $("#category option:selected").text(),
        "reSubmit": sumbitFlag
    };
    
    fetch(`${window.origin}/search`, {
        method: "POST",
        credentials: "include",
        body: JSON.stringify(formData),
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => {
        if (response.status !== 200) {
            console.log(`Response status was not 200: ${response.status}`);
            return
        }

        response.json().then((data) => {
            let cardContainer = document.getElementById("card-container");

            let html = "";

            if (data.hasOwnProperty("misSpelling")) {
                let correct = data["correctSpelling"];
                let misspelling = data["misSpelling"];
                html += `
                <div class="mt-2 ml-4">
                    <div><h5>Showing results for <span id="correct" onClick="reSubmit('${correct}');">${data["correctSpelling"]}</span></h5></div>
                    <div><p>Search instead for <span id="misspelling" onClick="reSubmit('${misspelling}');">${data["misSpelling"]}</span></p></div>
                </div>
                `;
            }

            data = data.data;
            for(let i = 0; i < data.length; i++) {
                let itemObj = data[i];


                if (itemObj.description.split(" ").length > 40) {
                    let description = itemObj.description.split(" ").slice(0, 39);
                    itemObj.description = description.join(" ");
                    itemObj.description += "...";
                }

                html += `
                <div class="card p-2 m-4 bg-white rounded" style="box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;">
                    <div class="col-md-9">
                        <div class="card-body">
                            <h5 class="card-title">
                                <a href="${itemObj.url}" target="_blank" style="color: black">
                                    ${itemObj.title}
                                </a>
                            </h5>
                            <a href="${itemObj.url}" target="_blank">${itemObj.url}</a>
                            <p class="card-text text-info" style="word-break: break-all"><b>id:</b> ${itemObj.id}</p>
                            <p class="card-text text-muted">${itemObj.description}</p>
                        </div>
                    </div>
                </div>
                `;
            }

            if (html === "") {
                html += "<div class='d-flex justify-content-center mt-5'><h5>No Search Results !</h5><div>";
            }
            cardContainer.innerHTML = html;
        });
    });
}