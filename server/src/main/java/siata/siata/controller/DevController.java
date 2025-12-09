package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.User;
import siata.siata.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/simulate-role")
    @PreAuthorize("isAuthenticated()") // Allow request to reach here for debugging
    public ResponseEntity<?> simulateRole(@RequestBody Map<String, String> payload, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        System.out.println("DEV SIMULATION DEBUG:");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Real DB Role: " + user.getRole());
        System.out.println("Effective Role: " + user.getEffectiveRole());
        System.out.println("Authorities: " + user.getAuthorities());
        
        // Recovery Logic:
        // If the user's DB role was accidentally overwritten, they lose ROLE_DEV.
        // We check if they have a 'simulationRole' set. Only DEVs have this field populated.
        // Or if they are currently authenticated as the role they simulated.
        
        boolean hasDevAuthority = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEV"));
        
        boolean isDevStr = "DEV".equals(user.getRole());
        
        // If simulationRole is not null, it means this user used the simulation feature, so they must be a DEV.
        boolean isLockedOutDev = user.getSimulationRole() != null && !user.getSimulationRole().isEmpty();

        if (!hasDevAuthority && !isDevStr && !isLockedOutDev) {
            return ResponseEntity.status(403).body("Access Denied: You are not recognized as DEV.");
        }
        
        return userRepository.findById(user.getUsername()).map(dbUser -> {
            String newRole = payload.get("role");
            
            // Safety: If this is a recovery (Locked Out Dev), force restore the real role to DEV
            if (isLockedOutDev && !"DEV".equals(dbUser.getRole())) {
                System.out.println("RECOVERING LOCKED OUT DEV: Restoring role to DEV.");
                dbUser.setRole("DEV");
            }

            // Normal Simulation Logic
            if (newRole == null || "DEV".equals(newRole)) {
                dbUser.setSimulationRole(null);
                // Also restore role if it was changed
                dbUser.setRole("DEV"); 
            } else {
                dbUser.setSimulationRole(newRole);
            }
            
            userRepository.save(dbUser);
            return ResponseEntity.ok("Simulation role updated to: " + (newRole == null ? "None" : newRole));
        }).orElse(ResponseEntity.notFound().build());
    }
}
