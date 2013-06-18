package kanbannow.core;

public class Card {

    private long id;
    private String cardText;
    private String postponedDate;

    public Card() {

    }


    public void setId(long anId) {
        this.id = anId;
    }



    public long getId() {
        return id;
    }


    public String getCardText() {
        return cardText;
    }

    public void setCardText(String aCardText) {
        this.cardText = aCardText;
    }

    // TODO:  Currently returns string formatted mm/dd/yyyy
    // Probably better to return Date and let caller format as desired
    public String getPostponedDate() {
        return postponedDate;
    }

    public void setPostponedDate(String aPostponedDate) {
        this.postponedDate = aPostponedDate;
    }
}
