package ar.edu.utn.frc.tup.lciii.services.impl;

import ar.edu.utn.frc.tup.lciii.dtos.match.MatchResponseDTO;
import ar.edu.utn.frc.tup.lciii.dtos.match.NewMatchRequestDTO;
import ar.edu.utn.frc.tup.lciii.dtos.play.PlayRequestDTO;
import ar.edu.utn.frc.tup.lciii.dtos.play.PlayResponseDTO;
import ar.edu.utn.frc.tup.lciii.entities.MatchEntity;
import ar.edu.utn.frc.tup.lciii.models.*;
import ar.edu.utn.frc.tup.lciii.repositories.jpa.MatchJpaRepository;
import ar.edu.utn.frc.tup.lciii.services.DeckService;
import ar.edu.utn.frc.tup.lciii.services.MatchService;
import ar.edu.utn.frc.tup.lciii.services.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchJpaRepository matchJpaRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private DeckService deckService;

    @Override
    public List<MatchResponseDTO> getMatchesByPlayer(Long playerId) {
        List<MatchResponseDTO> matches = new ArrayList<>();
        // TODO: Implementar el metodo de manera tal que retorne todas las partidas
        //  en las que haya participado un jugador

        Optional<List<MatchEntity>> list = matchJpaRepository.getAllByPlayerOneOrPlayerTwo(playerId);
        if(list.isPresent()){
            list.get().forEach(me ->{matches.add(modelMapper.map(me,MatchResponseDTO.class));});
            return matches;
        }
        else {
            throw new EntityNotFoundException("no hay matches pa");
        }
    }

    @Override
    public MatchResponseDTO createMatch(NewMatchRequestDTO newMatchRequestDTO) {
        Player player1 = playerService.getPlayerById(newMatchRequestDTO.getPlayerOneId());
        Player player2 = playerService.getPlayerById(newMatchRequestDTO.getPlayerTwoId());
        // TODO: Terminar de implementar el metodo de manera tal que cree un Match nuevo entre dos jugadores.
        //  Si alguno de los jugadores no existe, la partida no puede iniciarse y debe retornarse una excepcion del tipo
        //  EntityNotFoundException con el mensaje "The user {userId} do not exist"
        //  Cuando se cre el Match, debe crearse el mazo (DeckService.createDeck) y mesclarlo (DeckService.shuffleDeck)
        //  El Match siempre arranca con el playerOne iniciando la partida, con el indice 1 nextCardIndex y lastCard
        //  con la primera carta del mazo y con status PLAYING

        //Si alguno de los jugadores no existe, la partida no puede iniciarse y debe retornarse una excepcion del tipo
        //EntityNotFoundException con el mensaje "The user {userId} do not exist"
        if (player1 == null){
            throw new EntityNotFoundException("The user"+ player1.getId() + "does not exist");
        }
        if (player2 == null){
            throw new EntityNotFoundException("The user"+ player2.getId() + "does not exist");
        }

        //  Cuando se cre el Match, debe crearse el mazo (DeckService.createDeck) y mesclarlo (DeckService.shuffleDeck)
        Deck deck = deckService.createDeck();
        deckService.shuffleDeck(deck);
        Match match = new Match();

        //  El Match siempre arranca con el playerOne iniciando la partida, con el indice 1 nextCardIndex y lastCard
        //  con la primera carta del mazo y con status PLAYING

        match.setPlayerOne(player1);
        match.setPlayerTwo(player2);
        match.setNextToPlay(player1);
        match.setNextCardIndex(1);
        match.setLastCard(deck.getCards().get(match.getNextCardIndex()));
        match.setMatchStatus(MatchStatus.PLAYING);

        MatchEntity matchEntitySaved = matchJpaRepository.save(modelMapper.map(match,MatchEntity.class));

        return modelMapper.map(matchEntitySaved, MatchResponseDTO.class);
    }

    @Override
    public Match getMatchById(Long id) {
        MatchEntity me = matchJpaRepository.getReferenceById(id);
        if(me != null) {
            Match match = modelMapper.map(me, Match.class);
            return match;
        }else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    public MatchResponseDTO getMatchResponseDTOById(Long id) {
        MatchEntity me = matchJpaRepository.getReferenceById(id);
        if(me != null) {
            return modelMapper.map(me, MatchResponseDTO.class);
        }else {
            throw new EntityNotFoundException();
        }
    }

    @Transactional
    @Override
    public PlayResponseDTO play(Long matchId, PlayRequestDTO play) {
        PlayResponseDTO playResponseDTO = new PlayResponseDTO();
        Match match = this.getMatchById(matchId);
        // TODO: Terminar de implementar el metodo de manera tal que se ejecute la jugada siguiendo estas reglas:
        //  1 - Si el match no existe disparar una excepcion del tipo EntityNotFoundException
        //      con el mensaje "The match {matchId} do not exist"

        if(match == null){
            throw new EntityNotFoundException("The match"+ matchId +"do not exist");
        }

        //  2 - Si el jugador no existe disparar una excepcion del tipo EntityNotFoundException
        //      con el mensaje "The user {userId} do not exist"

        if(play == null){
            throw new EntityNotFoundException("The user"+ play.getPlayer() +"do not exist");
        }

        //  3 - Si el match ya terminó, disparar una excepcion del tipo MethodArgumentNotValidException
        //      con el mensaje "Game {gameId} is over"

        if(match.getMatchStatus() == MatchStatus.FINISH){
            throw new EntityNotFoundException("Game" + match.getId() +"is over");
        }

        //  4 - Si el jugador que manda la jugada no es el proximo a jugar, disparar una excepcion del tipo MethodArgumentNotValidException
        //      con el mensaje "It is not the turn of the user {userName}"

        Player playerSended = playerService.getPlayerById(play.getPlayer());

        if(match.getNextToPlay().getId() != play.getPlayer()){
            throw new EntityNotFoundException("It is not the turn of the user" + playerSended.getUserName());
        }

        //  5 - Si está OK, ejecutar la jugada haciendo lo siguiente:
        //      5.1 - Tomar el mazo de la partida y buscar la carta que sigue. Usar el metodo DeckService.takeCard



        Card nextCard = deckService.takeCard(match.getDeck(),match.getNextCardIndex());


        //      5.2 - Comparar si la carta tomada del mazo es mayor o menor que la ultima carta que se uso.
                      //Usar el metodo privado compareCards() de esta clase.
        Integer mayorMenor = compareCards(nextCard ,match.getLastCard());

        //
        //      5.3 - Comparar si el resultado de la comparacion de las cartas se condice con la decición del jugador

        if(play.getDecision() == PlayDecision.MAJOR && mayorMenor == 1 || play.getDecision() == PlayDecision.MINOR && mayorMenor == -1
            || mayorMenor == 0){

            //      5.4 - Si la respuesta es correcta (coinciden) el juego sigue y se debe actualizar
            //            la ultima carta recogida, el proximo jugador en jugar y el proximo indice de carta a recoger

            match.setNextCardIndex(match.getNextCardIndex()+1);
            match.setLastCard(nextCard);
            if(match.getNextToPlay() == match.getPlayerOne()){
                match.setNextToPlay(match.getPlayerTwo());
            }
            else {
                match.setNextToPlay(match.getPlayerOne());
            }
        }
        else {
            //      5.5 - Si la respuesta es incorrecta (no coincide) el juego termina y se debe actualizar
            //            la ultima carta recogida, el proximo jugador en jugar, el proximo indice de carta a recoger, el ganador
            //            y el estado de la partida

            match.setMatchStatus(MatchStatus.FINISH);
            match.setLastCard(nextCard);
            if(match.getNextToPlay() == match.getPlayerOne()){
                match.setNextToPlay(match.getPlayerTwo());
            }
            else {
                match.setNextToPlay(match.getPlayerOne());
            }
            match.setNextCardIndex(nextCard.getNumber());
            if(match.getNextToPlay() == match.getPlayerOne()){
                match.setWinner(match.getPlayerOne());
            }
            else {
                match.setNextToPlay(match.getPlayerTwo());
            }

        }



        //      5.6 - Actualizar el Match
        matchJpaRepository.save(modelMapper.map(match,MatchEntity.class));



        //  6 - Como respuesta, se deben completar los datos de PlayResponseDTO y retornarlo.

        playResponseDTO.setMatchStatus(match.getMatchStatus());
        playResponseDTO.setDecision(play.getDecision());
        if(match.getNextToPlay() == match.getPlayerOne()){
            playResponseDTO.setPlayer(match.getPlayerTwo().getId());
        }
        else {
            playResponseDTO.setPlayer(match.getPlayerOne().getId());
        }

        playResponseDTO.setYourCard(nextCard);
        playResponseDTO.setPreviousCard(match.getLastCard());// esta seteada en la linea antes de pedir la nueva carta

        playResponseDTO.setCardsInDeck(match.getDeck().getCards().size() - match.getNextCardIndex());

        return playResponseDTO;
    }

    private Integer compareCards(Card card1, Card card2) {
        // TODO: Implementr el metodo de manera tal que retorne:
        //  1 si card1 tiene un valor mayor que card2,
        if(card1.getNumber() > card2.getNumber()){
            return 1;
        }
        //  0 si card1 y card2 tienen el mismo valor,
        if(card1.getNumber() == card2.getNumber()){
            return 0;
        }
        //  -1 si card1 tiene un valor menor que card 2

        return  -1;
    }
}
