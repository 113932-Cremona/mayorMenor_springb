package ar.edu.utn.frc.tup.lciii.services.impl;

import ar.edu.utn.frc.tup.lciii.dtos.player.PlayerResponseDTO;
import ar.edu.utn.frc.tup.lciii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.lciii.models.Player;
import ar.edu.utn.frc.tup.lciii.services.LoginService;
import ar.edu.utn.frc.tup.lciii.services.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public Player login(String userName, String password) {
        // TODO: Implementar metodo de manera tal que permita a un usuario loguearse en la plataforma

        PlayerResponseDTO playerResponseDTO = playerService.getPlayerByUserNameAndPassword(userName,password);
        if(playerResponseDTO == null){
            throw new EntityNotFoundException("usuoario or password invalid");
        }
        else {

            return modelMapper.map(playerResponseDTO,Player.class);
        }

    }
}
